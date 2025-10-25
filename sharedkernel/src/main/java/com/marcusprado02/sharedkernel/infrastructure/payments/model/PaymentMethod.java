package com.marcusprado02.sharedkernel.infrastructure.payments.model;

import java.util.Map;

public record PaymentMethod(
        MethodType type, // CARD_TOKEN, PIX, BOLETO, WALLET, BANK_REDIRECT...
        String token,    // nunca guardar PAN direto; token do provider/vault
        Map<String, Object> details // ex.: last4, brand, exp, network token flags
) {}
