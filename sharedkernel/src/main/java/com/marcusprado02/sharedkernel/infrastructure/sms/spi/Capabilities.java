package com.marcusprado02.sharedkernel.infrastructure.sms.spi;

/** Capacidades suportadas por um provedor SMS específico. */
public record Capabilities(
        boolean supportsDLR,              // suporta delivery reports?
        boolean supportsLongSms,          // suporta mensagens concatenadas?
        boolean supportsUnicode,          // suporta UCS2 além de GSM-7?
        boolean supportsConcatenation     // suporta segmentação/concatenação
) {}