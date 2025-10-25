package com.marcusprado02.sharedkernel.crosscutting.formatters.number;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

import com.marcusprado02.sharedkernel.crosscutting.formatters.core.Formatter;

public final class CurrencyFormatter implements Formatter<BigDecimal> {
    private final NumberFormat nf;
    public CurrencyFormatter(Locale locale, Currency currency) {
        nf = NumberFormat.getCurrencyInstance(locale);
        nf.setCurrency(currency);
    }
    @Override
    public String format(BigDecimal v) { return nf.format(v); }
}

