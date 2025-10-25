package com.marcusprado02.sharedkernel.adapters.in.rest.dto;

import java.time.Instant;
import java.util.Map;

/** Metadados comuns no envelope de resposta. */
public record Meta(
        String requestId,
        String correlationId,
        String contract,
        Instant ts,
        String etag,
        Map<String, Object> extras
) {
    public static Meta of(String rid, String cid, String contract, String etag, Map<String,Object> extras){
        return new Meta(rid, cid, contract, Instant.now(), etag, extras==null?Map.of():extras);
    }
}