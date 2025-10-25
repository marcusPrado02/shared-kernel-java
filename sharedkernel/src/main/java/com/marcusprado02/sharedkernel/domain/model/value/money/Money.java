package com.marcusprado02.sharedkernel.domain.model.value.money;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

import com.marcusprado02.sharedkernel.domain.model.value.AbstractValueObject;

public final class Money extends AbstractValueObject {

    private final BigDecimal amount; // valor normalizado
    private final Currency currency;

    private static final int SCALE = 2; // ajuste por moeda em infra, se quiser
    private static final RoundingMode ROUND = RoundingMode.HALF_EVEN;

    public Money(BigDecimal amount, Currency currency) {
        this.currency = req(currency, "currency");
        // Normalização canônica (ex.: 2 casas); pode ser por moeda via registry
        this.amount = req(amount, "amount").setScale(SCALE, ROUND).stripTrailingZeros();
    }

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }
    public static Money of(double amount, String currencyCode) {
        return new Money(BigDecimal.valueOf(amount), Currency.getInstance(currencyCode));
    }
    public static Money of(BigDecimal amount, String currencyCode) {
        return new Money(amount, Currency.getInstance(currencyCode));
    }

    public BigDecimal amount() { return amount; }
    public Currency currency() { return currency; }

    public Money plus(Money other) {
        ensureSameCurrency(other);
        return new Money(this.amount.add(other.amount), currency);
    }

    public Money minus(Money other) {
        ensureSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), currency);
    }

    public Money times(BigDecimal factor) {
        return new Money(this.amount.multiply(req(factor, "factor")), currency);
    }

    public Money allocate(int parts) {
        if (parts <= 0) throw new IllegalArgumentException("parts must be > 0");
        BigDecimal[] div = amount.divideAndRemainder(BigDecimal.valueOf(parts));
        // Estratégia simples de distribuição; pode evoluir (banker’s rounding)
        return new Money(div[0], currency);
    }

    public int compareTo(Money other) {
        ensureSameCurrency(other);
        return this.amount.compareTo(other.amount);
    }

    private void ensureSameCurrency(Money other) {
        if (!this.currency.equals(req(other, "other").currency)) {
            throw new IllegalArgumentException("currency mismatch: %s vs %s"
                    .formatted(this.currency, other.currency));
        }
    }

    @Override protected Object[] equalityComponents() {
        return new Object[]{ amount, currency };
    }
}

