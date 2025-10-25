package com.marcusprado02.sharedkernel.infrastructure.payments.spi;


import java.util.Set;

public record Capabilities(
        boolean supports3DS, boolean supportsPartialCapture, boolean supportsRefund,
        boolean supportsPayout, Set<String> methods // "PIX","BOLETO","CARD_TOKEN",...
) {}
