package com.marcusprado02.sharedkernel.crosscutting.parser.text;

import com.marcusprado02.sharedkernel.crosscutting.parser.core.ParseError;
import com.marcusprado02.sharedkernel.crosscutting.parser.core.ParseResult;
import com.marcusprado02.sharedkernel.crosscutting.parser.core.Parser;

// text/BooleanLenientParser.java   // aceita yes/no, true/false, on/off, 1/0
public final class BooleanLenientParser implements Parser<Boolean> {
    @Override public ParseResult<Boolean> parse(String s) {
        var v = s.trim().toLowerCase(java.util.Locale.ROOT);
        return switch (v) {
            case "true","1","yes","y","on" -> ParseResult.ok(true);
            case "false","0","no","n","off" -> ParseResult.ok(false);
            default -> ParseResult.err(ParseError.of("Invalid boolean", 0, s, "Use true/false, yes/no, on/off, 1/0", null));
        };
    }
}

