package com.marcusprado02.sharedkernel.crosscutting.parser.number;

import com.marcusprado02.sharedkernel.crosscutting.parser.core.ParseError;
import com.marcusprado02.sharedkernel.crosscutting.parser.core.ParseResult;
import com.marcusprado02.sharedkernel.crosscutting.parser.core.Parser;

// number/PercentageParser.java   // aceita "25%", "0.25", "25,0%" (Locale)
public final class PercentageParser implements Parser<Double> {
    private final java.text.NumberFormat nf;
    public PercentageParser(java.util.Locale locale, boolean expectsSymbol) {
        nf = expectsSymbol ? java.text.NumberFormat.getPercentInstance(locale)
                           : java.text.NumberFormat.getNumberInstance(locale);
        nf.setMaximumFractionDigits(6);
    }
    @Override public ParseResult<Double> parse(String s) {
        try {
            var n = nf.parse(s).doubleValue();
            return ParseResult.ok((nf instanceof java.text.DecimalFormat df && df.toPattern().contains("%")) ? n : n);
        } catch (Exception e) {
            return ParseResult.err(ParseError.of("Invalid percentage", 0, s, "Ex.: 25% ou 0,25", e));
        }
    }
}

