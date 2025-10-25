package com.marcusprado02.sharedkernel.domain.model.value.money;


import java.util.Currency;
import java.util.Objects;

import com.marcusprado02.sharedkernel.domain.model.value.AbstractValueObject;

/**
 * VO que encapsula código ISO-4217 de moeda.
 * Validação rigorosa e imutabilidade.
 */
public final class CurrencyCode extends AbstractValueObject {

    private final Currency currency;

    private CurrencyCode(Currency currency) {
        this.currency = Objects.requireNonNull(currency, "currency must not be null");
    }

    public static CurrencyCode of(String code) {
        Objects.requireNonNull(code, "currency code must not be null");
        return new CurrencyCode(Currency.getInstance(code.toUpperCase()));
    }

    public static CurrencyCode of(Currency currency) {
        return new CurrencyCode(currency);
    }

    public String code() { return currency.getCurrencyCode(); }
    public int numericCode() { return currency.getNumericCode(); }
    public int defaultFractionDigits() { return currency.getDefaultFractionDigits(); }
    public Currency unwrap() { return currency; }

    @Override
    protected Object[] equalityComponents() { return new Object[]{ currency }; }

    @Override
    public String toString() { return code(); }
}
