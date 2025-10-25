package com.marcusprado02.sharedkernel.infrastructure.payments.model;

import java.math.BigDecimal;
import java.util.Currency;

public record Money(BigDecimal amount, Currency currency) {
    public Money {
        if (amount == null || currency == null) throw new IllegalArgumentException("null");
        // scale típica (até 3 casas cobrem JPY=0, KWD=3); ajuste conforme necessidade:
        amount = amount.stripTrailingZeros();
    }
}
