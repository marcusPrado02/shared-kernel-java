package com.marcusprado02.sharedkernel.crosscutting.parser.text;

import com.marcusprado02.sharedkernel.crosscutting.parser.core.ParseError;
import com.marcusprado02.sharedkernel.crosscutting.parser.core.ParseResult;
import com.marcusprado02.sharedkernel.crosscutting.parser.core.Parser;

// text/EnumParser.java
public final class EnumParser<E extends Enum<E>> implements Parser<E> {
    private final Class<E> enumType;
    private final boolean ignoreCase;
    public EnumParser(Class<E> enumType, boolean ignoreCase) {
        this.enumType = enumType; this.ignoreCase = ignoreCase;
    }
    @Override public ParseResult<E> parse(String s) {
        for (E c : enumType.getEnumConstants()) {
            if ((ignoreCase && c.name().equalsIgnoreCase(s)) || c.name().equals(s)) return ParseResult.ok(c);
        }
        return ParseResult.err(ParseError.of("Invalid enum constant", 0, s, "Valores: "+java.util.Arrays.toString(enumType.getEnumConstants()), null));
    }
}

