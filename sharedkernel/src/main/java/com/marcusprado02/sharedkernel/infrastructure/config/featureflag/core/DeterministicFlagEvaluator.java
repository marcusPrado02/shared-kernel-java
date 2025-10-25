package com.marcusprado02.sharedkernel.infrastructure.config.featureflag.core;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.EvaluationDetail;
import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.FlagContext;
import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.model.Condition;
import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.model.FlagDefinition;
import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.model.Operator;
import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.model.Rollout;
import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.model.Variant;

public final class DeterministicFlagEvaluator implements FlagEvaluator {
  @Override
  @SuppressWarnings("unchecked")
  public <T> EvaluationDetail<T> evaluate(FlagDefinition def, Class<T> type, T defaultValue, FlagContext ctx) {
    if (!def.enabled()) return fallthrough(def, type, defaultValue, "OFF");
    // prerequisites
    for (var pre : def.prerequisites()) {
      // Observe: chama o próprio client em produção via um "self" ou injeta um resolver de prereq
      // Aqui assume-se que provider já garante consistência ou que prereq foi resolvido a montante.
    }
    // rules
    for (var rule : def.rules()) {
      if (matches(rule.when(), ctx)) {
        var chosen = pickVariant(def, rule.rollout(), ctx);
        return detail(def, type, defaultValue, chosen, "RULE_MATCH:" + rule.id());
      }
    }
    // fallthrough default
    var v = def.defaultVariant();
    return detail(def, type, defaultValue, v, "FALLTHROUGH");
  }

  private boolean matches(List<Condition> conds, FlagContext ctx) {
    for (var c : conds) {
      var val = resolveAttr(ctx, c.attribute());
      if (!op(c.op(), val, c.value())) return false;
    }
    return true;
  }

  private Object resolveAttr(FlagContext ctx, String attr) {
    return switch (attr) {
      case "tenantId" -> ctx.tenantId();
      case "userId" -> ctx.userId();
      case "region" -> ctx.region();
      default -> ctx.attrs() == null ? null : ctx.attrs().get(attr);
    };
  }
  private boolean op(Operator op, Object left, Object right) {
    if (op == Operator.EXISTS) return left != null;
    if (left == null) return false;
    return switch (op) {
      case EQ -> left.equals(right);
      case NEQ -> !left.equals(right);
      case IN -> right instanceof Collection<?> col && col.contains(left);
      case NOT_IN -> right instanceof Collection<?> col && !col.contains(left);
      case GT, GTE, LT, LTE -> compareNums(op, left, right);
      case MATCHES -> left.toString().matches(right.toString());
      case STARTS_WITH -> left.toString().startsWith(right.toString());
      case ENDS_WITH -> left.toString().endsWith(right.toString());
      case CONTAINS -> left.toString().contains(right.toString());
      default -> false;
    };
  }
  private boolean compareNums(Operator op, Object l, Object r) {
    double a = Double.parseDouble(l.toString()), b = Double.parseDouble(r.toString());
    return switch (op) {
      case GT -> a > b; case GTE -> a >= b; case LT -> a < b; case LTE -> a <= b;
      default -> false;
    };
  }

  private Variant pickVariant(FlagDefinition def, Rollout rollout, FlagContext ctx) {
    if (rollout == null || rollout.buckets().isEmpty()) return def.defaultVariant();
    var seedAttr = rollout.buckets().get(0).seedAttribute();
    var seed = Objects.toString(resolveAttr(ctx, seedAttr), "null");
    int bucket = hash(seed + ":" + def.key()) % 10000; // 0..9999
    int acc = 0;
    for (var b : rollout.buckets()) {
      acc += b.weight();
      if (bucket < acc) return def.variants().get(b.variant());
    }
    return def.defaultVariant();
  }
  private int hash(String s) {
    try {
      var md = MessageDigest.getInstance("SHA-256");
      var bytes = md.digest(s.getBytes(StandardCharsets.UTF_8));
      // 2 bytes p/ 0..65535
      return ((bytes[0] & 0xff) << 8 | (bytes[1] & 0xff)) % 10000;
    } catch (Exception e) { return 0; }
  }

  @SuppressWarnings("unchecked")
  private <T> EvaluationDetail<T> detail(FlagDefinition def, Class<T> type, T fallback, Variant v, String reason) {
    if (v == null) return new EvaluationDetail<>(fallback, def.key(), "default", reason, true, Map.of());
    Object raw = v.value();
    try {
      T casted = cast(raw, type, fallback);
      return new EvaluationDetail<>(casted, def.key(), v.name(), reason, false, Map.of("version", def.version()));
    } catch (ClassCastException ex) {
      return new EvaluationDetail<>(fallback, def.key(), "default", "TYPE_MISMATCH", true, Map.of("expected", type.getSimpleName()));
    }
  }
  private <T> T cast(Object raw, Class<T> type, T fallback) {
    if (raw == null) return fallback;
    if (type.isInstance(raw)) return type.cast(raw);
    if (type == Boolean.class) return type.cast(Boolean.valueOf(raw.toString()));
    if (type == Long.class)    return type.cast(Long.valueOf(raw.toString()));
    if (type == Double.class)  return type.cast(Double.valueOf(raw.toString()));
    if (type == String.class)  return type.cast(raw.toString());
    throw new ClassCastException();
  }

  private <T> EvaluationDetail<T> fallthrough(FlagDefinition def, Class<T> type, T defaultValue, String reason) {
    return new EvaluationDetail<>(defaultValue, def.key(), "default", reason, true, Map.of("version", def.version()));
  }
}