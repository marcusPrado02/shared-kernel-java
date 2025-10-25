package com.marcusprado02.sharedkernel.crosscutting.parser.time;

import com.marcusprado02.sharedkernel.crosscutting.parser.core.ParseError;
import com.marcusprado02.sharedkernel.crosscutting.parser.core.ParseResult;
import com.marcusprado02.sharedkernel.crosscutting.parser.core.Parser;

// time/Iso8601InstantParser.java
public final class Iso8601InstantParser implements Parser<java.time.Instant> {
    private final java.time.format.DateTimeFormatter fmt;
    public Iso8601InstantParser() { this.fmt = java.time.format.DateTimeFormatter.ISO_DATE_TIME; }
    @Override public ParseResult<java.time.Instant> parse(String s) {
        try {
            var t = java.time.OffsetDateTime.parse(s, fmt).toInstant();
            return ParseResult.ok(t);
        } catch (Exception e) {
            return ParseResult.err(ParseError.of("Invalid ISO-8601 instant", 0, safeSnippet(s), 
                "Ex.: 2025-09-04T21:30:00-03:00", e));
        }
    }
    private String safeSnippet(String s) { return s == null ? "null" : s.length()>64 ? s.substring(0,64)+"…" : s; }
}

