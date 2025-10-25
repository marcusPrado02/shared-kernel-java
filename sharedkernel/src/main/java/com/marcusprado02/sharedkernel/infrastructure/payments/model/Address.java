package com.marcusprado02.sharedkernel.infrastructure.payments.model;

public record Address(
        String line1,
        String line2,
        String city,
        String state,
        String postalCode,
        String country
) {}
