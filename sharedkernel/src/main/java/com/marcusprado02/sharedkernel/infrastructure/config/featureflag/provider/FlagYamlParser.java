package com.marcusprado02.sharedkernel.infrastructure.config.featureflag.provider;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.model.Bucket;
import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.model.Condition;
import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.model.FlagDefinition;
import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.model.Operator;
import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.model.Prerequisite;
import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.model.Rollout;
import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.model.Rule;
import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.model.Variant;

/**
 * Parser de YAML -> Map<String, FlagDefinition>.
 * Estrutura esperada:
 *   root:
 *     version: string|number (opcional; convertido para long se número, parse best-effort se string)
 *     flags: { <key>: { ...FlagDefinition-like... }, ... }
 *
 * Notas:
 * - Converte tipos básicos (BOOL/STRING/NUMBER/JSON) para o campo value de Variant.
 * - É tolerante quanto a ausências (enabled default true, version default 0, etc.).
 * - Mantém a ordem de inserção (LinkedHashMap) para previsibilidade.
 */
public final class FlagYamlParser {

  private FlagYamlParser() {}

  @SuppressWarnings("unchecked")
  public static Map<String, FlagDefinition> parse(Reader yamlReader) {
    if (yamlReader == null) return Map.of();

    Yaml yaml = new Yaml();
    Object rootObj = yaml.load(yamlReader);
    if (!(rootObj instanceof Map)) return Map.of();

    Map<String, Object> root = (Map<String, Object>) rootObj;
    long globalVersion = parseVersion(root.get("version"));

    Object flagsObj = root.get("flags");
    if (!(flagsObj instanceof Map)) return Map.of();

    Map<String, Object> flags = (Map<String, Object>) flagsObj;
    Map<String, FlagDefinition> out = new LinkedHashMap<>();

    for (Map.Entry<String, Object> e : flags.entrySet()) {
      String key = e.getKey();
      if (!(e.getValue() instanceof Map)) continue;

      Map<String, Object> node = (Map<String, Object>) e.getValue();

      String description = asString(node.get("description"));
      boolean enabled = getBoolean(node.get("enabled"), true);
      long version = parseVersion(node.get("version"));
      if (version == 0L) version = globalVersion; // herda version root se não houver

      // prerequisites
      List<Prerequisite> prerequisites = new ArrayList<>();
      for (Map<String, Object> pre : asListOfMaps(node.get("prerequisites"))) {
        String f = asString(pre.get("flagKey"));
        String req = asString(pre.get("requiredVariant"));
        if (f != null && req != null) {
          prerequisites.add(new Prerequisite(f, req));
        }
      }

      // variants
      Map<String, Variant> variants = new LinkedHashMap<>();
      Object variantsObj = node.get("variants");
      if (variantsObj instanceof Map<?,?> vm) {
        for (Map.Entry<?,?> ve : vm.entrySet()) {
          String variantName = String.valueOf(ve.getKey());
          if (!(ve.getValue() instanceof Map<?,?> vmap)) continue;

          String type = normalizeType(asString(vmap.get("type"))); // BOOL|STRING|NUMBER|JSON
          Object rawValue = vmap.get("value");
          Object coerced = coerceVariantValue(type, rawValue);
          variants.put(variantName, new Variant(variantName, coerced, type));
        }
      }

      // defaultVariant (pode vir como {name,type,value} ou apenas {name: <x> ...})
      Variant defaultVariant = null;
      Object dvObj = node.get("defaultVariant");
      if (dvObj instanceof Map<?,?> dvm) {
        String dvName = asString(dvm.get("name"));
        String dvType = normalizeType(asString(dvm.get("type")));
        Object dvRaw = dvm.get("value");
        if (dvType == null && dvName != null && variants.containsKey(dvName)) {
          // herda do variants
          defaultVariant = variants.get(dvName);
        } else {
          Object dvCoerced = coerceVariantValue(dvType, dvRaw);
          defaultVariant = new Variant(dvName != null ? dvName : "default", dvCoerced, dvType != null ? dvType : inferType(dvCoerced));
        }
      } else if (dvObj instanceof String dvNameSimple && variants.containsKey(dvNameSimple)) {
        defaultVariant = variants.get(dvNameSimple);
      }
      if (defaultVariant == null && !variants.isEmpty()) {
        // se não definido, usa o primeiro variant como default por convenção
        Map.Entry<String, Variant> first = variants.entrySet().iterator().next();
        defaultVariant = first.getValue();
      }

      // rules
      List<Rule> rules = new ArrayList<>();
      for (Map<String, Object> r : asListOfMaps(node.get("rules"))) {
        String id = asString(r.get("id"));
        List<Condition> when = new ArrayList<>();
        for (Map<String, Object> c : asListOfMaps(r.get("when"))) {
          String attribute = asString(c.get("attribute"));
          String opStr = asString(c.get("op"));
          Operator op = parseOperator(opStr);
          Object value = c.get("value");
          if (attribute != null && op != null) {
            when.add(new Condition(attribute, op, value));
          }
        }

        // rollout
        Rollout rollout = null;
        Object ro = r.get("rollout");
        if (ro instanceof Map<?,?> rom) {
          List<Bucket> buckets = new ArrayList<>();
          for (Map<String, Object> b : asListOfMaps(((Map<String, Object>) rom).get("buckets"))) {
            String vname = asString(b.get("variant"));
            int weight = getInt(b.get("weight"), 0);
            String seedAttr = asString(b.get("seedAttribute"));
            if (vname != null) buckets.add(new Bucket(vname, weight, seedAttr != null ? seedAttr : "userId"));
          }
          rollout = buckets.isEmpty() ? null : new Rollout(Collections.unmodifiableList(buckets));
        }

        rules.add(new Rule(id != null ? id : "rule-" + (rules.size() + 1),
                           Collections.unmodifiableList(when),
                           rollout));
      }

      FlagDefinition def = new FlagDefinition(
          key,
          enabled,
          Collections.unmodifiableList(prerequisites),
          Collections.unmodifiableList(rules),
          defaultVariant,
          Collections.unmodifiableMap(variants),
          description,
          version
      );
      out.put(key, def);
    }

    return Collections.unmodifiableMap(out);
  }

  // ------------------- helpers -------------------

  private static long parseVersion(Object v) {
    if (v == null) return 0L;
    if (v instanceof Number n) return n.longValue();
    String s = String.valueOf(v).trim();
    // tenta extrair dígitos finais (ex.: "2025.10.02-001" -> 1) apenas para ordenar; se não, 0
    try {
      if (s.matches("\\d+")) return Long.parseLong(s);
    } catch (NumberFormatException ignored) {}
    return 0L;
  }

  private static String asString(Object o) {
    return o == null ? null : String.valueOf(o);
  }

  @SuppressWarnings("unchecked")
  private static List<Map<String, Object>> asListOfMaps(Object o) {
    if (!(o instanceof List<?> l)) return List.of();
    List<Map<String,Object>> out = new ArrayList<>();
    for (Object item : l) if (item instanceof Map<?,?> m) out.add((Map<String,Object>) m);
    return out;
  }

  private static boolean getBoolean(Object o, boolean def) {
    if (o == null) return def;
    if (o instanceof Boolean b) return b;
    String s = String.valueOf(o).toLowerCase(Locale.ROOT);
    if ("true".equals(s) || "yes".equals(s) || "on".equals(s)) return true;
    if ("false".equals(s) || "no".equals(s) || "off".equals(s)) return false;
    return def;
  }

  private static int getInt(Object o, int def) {
    if (o == null) return def;
    if (o instanceof Number n) return n.intValue();
    try { return Integer.parseInt(String.valueOf(o)); } catch (Exception e) { return def; }
  }

  private static String normalizeType(String t) {
    if (t == null) return null;
    String u = t.trim().toUpperCase(Locale.ROOT);
    return switch (u) {
      case "BOOL", "BOOLEAN" -> "BOOL";
      case "STRING", "STR" -> "STRING";
      case "NUMBER", "NUM", "INT", "LONG", "DOUBLE" -> "NUMBER";
      case "JSON", "OBJECT", "MAP", "ARRAY" -> "JSON";
      default -> u;
    };
  }

  private static Object coerceVariantValue(String type, Object raw) {
    if (type == null) {
      // tenta inferir
      if (raw instanceof Boolean) return raw;
      if (raw instanceof Number) return ((Number) raw).doubleValue();
      return raw; // String/Map/List etc.
    }
    return switch (type) {
      case "BOOL" -> toBool(raw);
      case "NUMBER" -> toNumber(raw);
      case "STRING" -> raw == null ? null : String.valueOf(raw);
      case "JSON" -> raw; // manter estrutura (Map/List)
      default -> raw;
    };
  }

  private static String inferType(Object v) {
    if (v instanceof Boolean) return "BOOL";
    if (v instanceof Number) return "NUMBER";
    if (v instanceof Map || v instanceof List) return "JSON";
    return "STRING";
  }

  private static Boolean toBool(Object raw) {
    if (raw == null) return null;
    if (raw instanceof Boolean b) return b;
    String s = String.valueOf(raw).toLowerCase(Locale.ROOT);
    return ("true".equals(s) || "yes".equals(s) || "on".equals(s));
  }

  private static Number toNumber(Object raw) {
    if (raw == null) return null;
    if (raw instanceof Number n) return n.doubleValue();
    try { return Double.parseDouble(String.valueOf(raw)); } catch (Exception e) { return null; }
  }

  private static Operator parseOperator(String s) {
    if (s == null) return null;
    String u = s.trim().toUpperCase(Locale.ROOT);
    // mapeia aliases comuns
    if ("IN".equals(u)) return Operator.IN;
    if ("NOT_IN".equals(u) || "NOTIN".equals(u)) return Operator.NOT_IN;
    if ("=".equals(u) || "EQ".equals(u)) return Operator.EQ;
    if ("!=".equals(u) || "NEQ".equals(u)) return Operator.NEQ;
    if (">".equals(u) || "GT".equals(u)) return Operator.GT;
    if (">=".equals(u) || "GTE".equals(u)) return Operator.GTE;
    if ("<".equals(u) || "LT".equals(u)) return Operator.LT;
    if ("<=".equals(u) || "LTE".equals(u)) return Operator.LTE;
    if ("MATCHES".equals(u) || "REGEX".equals(u)) return Operator.MATCHES;
    if ("STARTS_WITH".equals(u) || "STARTSWITH".equals(u)) return Operator.STARTS_WITH;
    if ("ENDS_WITH".equals(u) || "ENDSWITH".equals(u)) return Operator.ENDS_WITH;
    if ("CONTAINS".equals(u)) return Operator.CONTAINS;
    if ("EXISTS".equals(u)) return Operator.EXISTS;
    // fallback
    try { return Operator.valueOf(u); } catch (Exception e) { return null; }
  }
}

