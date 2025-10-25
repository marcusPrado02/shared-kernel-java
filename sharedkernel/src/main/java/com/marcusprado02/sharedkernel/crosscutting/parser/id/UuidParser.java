package com.marcusprado02.sharedkernel.crosscutting.parser.id;

import com.marcusprado02.sharedkernel.crosscutting.parser.core.*;

// id/UuidParser.java
public final class UuidParser implements Parser<java.util.UUID> {
    @Override public ParseResult<java.util.UUID> parse(String s) {
        try { return ParseResult.ok(java.util.UUID.fromString(s.trim())); }
        catch (Exception e) { return ParseResult.err(ParseError.of("Invalid UUID", firstBadPos(s), s, "Ex.: 123e4567-e89b-12d3-a456-426614174000", e)); }
    }
    private int firstBadPos(String s){ return 0; }
}

