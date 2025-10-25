package com.marcusprado02.sharedkernel.crosscutting.parser.money;

import com.marcusprado02.sharedkernel.crosscutting.parser.core.ParseError;
import com.marcusprado02.sharedkernel.crosscutting.parser.core.ParseResult;
import com.marcusprado02.sharedkernel.crosscutting.parser.core.Parser;
import com.marcusprado02.sharedkernel.domain.model.value.money.Money;
import java.math.BigDecimal;

public final class CurrencyAmountParser implements Parser<Money> {
    private final java.util.Locale locale;
    private final java.util.Currency defaultCurrency;
    public CurrencyAmountParser(java.util.Locale locale, java.util.Currency defaultCurrency) {
        this.locale = locale; this.defaultCurrency = defaultCurrency;
    }
    @Override public ParseResult<Money> parse(String s) {
        String in = s.trim();
        try {
            java.util.Currency cur = defaultCurrency;
            var iso = java.util.regex.Pattern.compile("^[A-Z]{3}"); var mIso = iso.matcher(in);
            if (mIso.find()) { cur = java.util.Currency.getInstance(in.substring(0,3)); in = in.substring(3).trim(); }
            var nf = java.text.NumberFormat.getCurrencyInstance(locale);
            nf.setCurrency(cur);
            var n = nf.parse(in).toString(); // usa parsing do locale
            return ParseResult.ok(new Money(new BigDecimal(n), cur));
        } catch (Exception e) {
            return ParseResult.err(ParseError.of("Invalid currency amount", 0, s, "Ex.: R$ 10,50 ou USD 10.50", e));
        }
    }
}
