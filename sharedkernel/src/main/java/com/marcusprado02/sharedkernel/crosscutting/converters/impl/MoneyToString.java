package com.marcusprado02.sharedkernel.crosscutting.converters.impl;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import java.math.BigDecimal;

import com.marcusprado02.sharedkernel.crosscutting.converters.core.*;
import com.marcusprado02.sharedkernel.domain.model.value.money.Money;

public final class MoneyToString implements BidiConverter<Money,String> {
    private final Locale locale;
    public MoneyToString(Locale locale) { this.locale = locale; }

    @Override public String convert(Money m) {
        var nf = NumberFormat.getCurrencyInstance(locale); 
        nf.setCurrency(m.currency());
        return nf.format(m.amount());
    }

    @Override public Converter<String,Money> inverse() {
        return s -> {
            try {
                // suporta "USD 10.50" ou "R$ 10,50"
                var trimmed = s.trim();
                Currency cur = detect(trimmed).orElseThrow(() -> new ConversionException("Currency missing"));
                var nf = NumberFormat.getCurrencyInstance(locale); nf.setCurrency(cur);
                Number n = nf.parse(trimmed.replaceFirst("^[A-Z]{3}", "").trim());
                return new Money(new BigDecimal(n.toString()), cur);
            } catch (Exception e) { throw new ConversionException("Invalid money: "+safe(s), e); }
        };
    }
    private Optional<Currency> detect(String s) {
        var m = Pattern.compile("^([A-Z]{3})").matcher(s);
        return m.find() ? Optional.of(Currency.getInstance(m.group(1))) : Optional.empty();
    }
    private String safe(String s){ return s==null?"null":s.length()>40?s.substring(0,40)+"…":s; }
}
