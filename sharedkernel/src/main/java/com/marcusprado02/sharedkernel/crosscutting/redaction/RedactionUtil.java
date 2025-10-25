package com.marcusprado02.sharedkernel.crosscutting.redaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.lang.reflect.RecordComponent;
import java.util.*;

/**
 * Redação de campos sensíveis em diferentes tipos de payload:
 * - JsonNode (ObjectNode)  -> mascara chaves especificadas
 * - Map<String,?>          -> mascara chaves especificadas
 * - POJO/record            -> fallback para árvore Jackson e retorna JsonNode mascarado
 *
 * Observação: Para POJO, retornamos um JsonNode mascarado para evitar mutação reflexiva.
 * Se quiser manter o mesmo tipo, crie um mapper específico (MixIn) e reconverta.
 */
public final class RedactionUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String MASK = "*****";

    private RedactionUtil() {}

    public static Object maskFields(Object payload, List<String> fields) {
        if (payload == null || fields == null || fields.isEmpty()) return payload;

        // JsonNode
        if (payload instanceof ObjectNode on) {
            maskObjectNode(on, new HashSet<>(fields));
            return on;
        }
        if (payload instanceof JsonNode node) {
            // cria cópia mutável
            ObjectNode copy = node.isObject() ? ((ObjectNode) node).deepCopy()
                                              : MAPPER.createObjectNode().set("value", node).deepCopy();
            maskObjectNode(copy, new HashSet<>(fields));
            return copy;
        }

        // Map
        if (payload instanceof Map<?, ?> map) {
            Map<String, Object> out = new LinkedHashMap<>();
            for (var e : map.entrySet()) {
                String k = String.valueOf(e.getKey());
                Object v = e.getValue();
                out.put(k, fields.contains(k) ? maskedValue(v) : v);
            }
            return out;
        }

        // Record/POJO -> para JsonNode, mascara e retorna JsonNode
        try {
            ObjectNode on = MAPPER.valueToTree(payload);
            maskObjectNode(on, new HashSet<>(fields));
            return on;
        } catch (IllegalArgumentException ignore) {
            // sem suporte: devolve original
            return payload;
        }
    }

    private static void maskObjectNode(ObjectNode obj, Set<String> fields) {
        Iterator<String> it = obj.fieldNames();
        List<String> names = new ArrayList<>();
        it.forEachRemaining(names::add);

        for (String name : names) {
            var value = obj.get(name);
            if (fields.contains(name)) {
                obj.put(name, MASK);
            } else if (value != null && value.isObject()) {
                maskObjectNode((ObjectNode) value, fields);
            } else if (value != null && value.isArray()) {
                for (int i = 0; i < value.size(); i++) {
                    JsonNode vi = value.get(i);
                    if (vi.isObject()) maskObjectNode((ObjectNode) vi, fields);
                }
            }
        }
    }

    private static Object maskedValue(Object v) {
        if (v == null) return null;
        if (v instanceof CharSequence) return MASK;
        if (v instanceof Number || v instanceof Boolean) return MASK;
        // Para estruturas, retorna string mascarada curta
        String s = String.valueOf(v);
        if (s.length() > 16) s = s.substring(0, 16) + "...";
        return MASK + "(" + s + ")";
    }
}
