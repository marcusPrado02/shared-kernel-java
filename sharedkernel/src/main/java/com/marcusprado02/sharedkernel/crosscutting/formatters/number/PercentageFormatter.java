package com.marcusprado02.sharedkernel.crosscutting.formatters.number;

import java.text.NumberFormat;
import java.util.Locale;

import com.marcusprado02.sharedkernel.crosscutting.formatters.core.Formatter;

public final class PercentageFormatter implements Formatter<Double> {
    private final NumberFormat nf;
    public PercentageFormatter(Locale locale, int maxFrac) {
        nf = NumberFormat.getPercentInstance(locale);
        nf.setMaximumFractionDigits(maxFrac);
    }
    @Override
    public String format(Double v) { return nf.format(v); }
}
