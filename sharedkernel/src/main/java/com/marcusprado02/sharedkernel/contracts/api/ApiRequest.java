package com.marcusprado02.sharedkernel.contracts.api;

import java.net.InetAddress;
import java.time.Instant;
import java.util.*;

/** Requisição canônica. Evite dependências de framework. */
public record ApiRequest(
        String method,
        String path,
        Map<String, List<String>> query,
        Map<String, String> headers,
        byte[] body,
        InetAddress remoteIp,
        Instant startedAt,
        Locale locale
) {
    public String header(String k) { return headers.getOrDefault(k, null); }
    public List<String> query(String k) { return query.getOrDefault(k, List.of()); }
}
