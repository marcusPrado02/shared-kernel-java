package com.marcusprado02.sharedkernel.cqrs.queryfilterbuilder;

import java.time.*;
import java.util.*;

public final class FieldRegistry {
    public record FieldMeta(String externalName, String internalPath, Class<?> type, Set<Operator> allowedOps) {}

    private final Map<String, FieldMeta> fields = new HashMap<>();
    private String tenantField;     // ex.: "tenantId"
    private String softDeleteField; // ex.: "deleted"

    public FieldRegistry allow(String external, String internalPath, Class<?> type, Operator... ops) {
        fields.put(external, new FieldMeta(external, internalPath, type, Set.of(ops)));
        return this;
    }

    public Optional<FieldMeta> meta(String external) { return Optional.ofNullable(fields.get(external)); }

    public FieldRegistry tenant(String external) { this.tenantField = external; return this; }
    public FieldRegistry softDelete(String external){ this.softDeleteField = external; return this; }

    public Optional<String> tenantField() { return Optional.ofNullable(tenantField); }
    public Optional<String> softDeleteField() { return Optional.ofNullable(softDeleteField); }
}
