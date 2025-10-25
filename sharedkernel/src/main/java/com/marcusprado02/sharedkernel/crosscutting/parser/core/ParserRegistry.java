package com.marcusprado02.sharedkernel.crosscutting.parser.core;

// core/ParserRegistry.java
public final class ParserRegistry {
    private final java.util.Map<Class<?>, Parser<?>> map = new java.util.concurrent.ConcurrentHashMap<>();
    public <T> void register(Class<T> type, Parser<T> parser) { map.put(type, parser); }

    @SuppressWarnings("unchecked")
    public <T> ParseResult<T> parse(Class<T> type, String input) {
        var p = (Parser<T>) map.get(type);
        return (p == null) ? ParseResult.err(ParseError.simple("No parser for "+type)) : p.parse(input);
    }
}
