package com.marcusprado02.sharedkernel.crosscutting.parser.number;

import com.marcusprado02.sharedkernel.crosscutting.parser.core.ParseError;
import com.marcusprado02.sharedkernel.crosscutting.parser.core.ParseResult;
import com.marcusprado02.sharedkernel.crosscutting.parser.core.Parser;

// number/IntegerParser.java
public final class IntegerParser implements Parser<Integer> {
    @Override public ParseResult<Integer> parse(String s) {
        try { return ParseResult.ok(Integer.parseInt(s.trim())); }
        catch (Exception e) { return ParseResult.err(ParseError.of("Invalid integer", firstBadPos(s), s, "Ex.: 42", e)); }
    }
    private int firstBadPos(String s) { for (int i=0;i<s.length();i++) if (!Character.isDigit(s.charAt(i)) && !(i==0 && s.charAt(i)=='-')) return i; return -1; }
}

