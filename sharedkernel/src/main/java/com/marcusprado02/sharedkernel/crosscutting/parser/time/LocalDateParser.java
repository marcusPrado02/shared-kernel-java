package com.marcusprado02.sharedkernel.crosscutting.parser.time;

import com.marcusprado02.sharedkernel.crosscutting.parser.core.ParseError;
import com.marcusprado02.sharedkernel.crosscutting.parser.core.ParseResult;
import com.marcusprado02.sharedkernel.crosscutting.parser.core.Parser;

// time/LocalDateParser.java
public final class LocalDateParser implements Parser<java.time.LocalDate> {
    private final java.time.format.DateTimeFormatter fmt;
    public LocalDateParser(java.time.format.DateTimeFormatter fmt) { this.fmt = fmt; }
    @Override public ParseResult<java.time.LocalDate> parse(String s) {
        try { return ParseResult.ok(java.time.LocalDate.parse(s, fmt)); }
        catch (Exception e) {
            return ParseResult.err(ParseError.of("Invalid date", 0, s, "Use o padrão: "+fmt, e));
        }
    }
}
