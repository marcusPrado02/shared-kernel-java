package com.marcusprado02.sharedkernel.adapters.in.rest.dto.example.impl;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.marcusprado02.sharedkernel.adapters.in.rest.dto.Redactable;
import com.marcusprado02.sharedkernel.adapters.in.rest.dto.ResponseDTO;

import java.time.Instant;
import java.util.Set;

/** Representação canônica, imutável. */
@JsonPropertyOrder({"id","version","name","email","region","tags","createdAt","updatedAt"})
public record CustomerRes(
        String id,
        long version,
        String name,
        String email, // PII -> pode ser mascarado
        String region,
        Set<String> tags,
        Instant createdAt,
        @JsonInclude(JsonInclude.Include.NON_NULL) Instant updatedAt
) implements ResponseDTO, Redactable {

    @Override public CustomerRes redacted() {
        // Mascarar email: jo***@dominio
        String masked = email==null? null : maskEmail(email);
        return new CustomerRes(id, version, name, masked, region, tags, createdAt, updatedAt);
    }

    private static String maskEmail(String e) {
        int at = e.indexOf('@');
        if (at<=1) return "***";
        return e.substring(0, Math.min(2, at)) + "***" + e.substring(at);
    }
}
