package com.marcusprado02.sharedkernel.crosscutting.parser.number;

import java.math.BigDecimal;

import com.marcusprado02.sharedkernel.crosscutting.parser.core.ParseError;
import com.marcusprado02.sharedkernel.crosscutting.parser.core.ParseResult;
import com.marcusprado02.sharedkernel.crosscutting.parser.core.Parser;

// number/BigDecimalParser.java  (locale-aware)
public final class BigDecimalParser implements Parser<BigDecimal> {
    private final java.text.DecimalFormat df;
    public BigDecimalParser(java.util.Locale locale, int fraction) {
        var nf = java.text.NumberFormat.getNumberInstance(locale);
        this.df = (java.text.DecimalFormat) nf;
        this.df.setParseBigDecimal(true);
        this.df.setMaximumFractionDigits(fraction);
    }
    @Override public ParseResult<BigDecimal> parse(String s) {
        try { return ParseResult.ok((BigDecimal) df.parse(s)); }
        catch (Exception e) { return ParseResult.err(ParseError.of("Invalid decimal", 0, s, "Respeite o Locale: "+df.getDecimalFormatSymbols().getDecimalSeparator(), e)); }
    }
}

