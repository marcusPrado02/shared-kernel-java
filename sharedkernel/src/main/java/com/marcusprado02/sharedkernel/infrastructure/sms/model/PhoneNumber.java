package com.marcusprado02.sharedkernel.infrastructure.sms.model;

public record PhoneNumber(String e164) {
    public PhoneNumber {
        if (e164 == null || !e164.startsWith("+")) throw new IllegalArgumentException("E.164 inválido");
    }
}
