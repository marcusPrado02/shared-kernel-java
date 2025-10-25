package com.marcusprado02.sharedkernel.crosscutting.parser.core;

// core/Combinators.java
public final class Combinators {
    private Combinators(){}

    // lista de valores separados por delimitador ("," por padrão)
    public static <T> Parser<java.util.List<T>> delimited(Parser<T> item, String delimiter, boolean allowSpaces) {
        return in -> {
            var parts = allowSpaces ? in.split("\\s*"+java.util.regex.Pattern.quote(delimiter)+"\\s*")
                                    : in.split(java.util.regex.Pattern.quote(delimiter));
            var out = new java.util.ArrayList<T>(parts.length);
            for (String p: parts) {
                var r = item.parse(p);
                if (!r.isOk()) return ParseResult.err(ParseError.of("Invalid delimited item", 0, p, "Verifique o item '"+p+"'", null));
                out.add(r.get());
            }
            return ParseResult.ok(java.util.Collections.unmodifiableList(out));
        };
    }

    // chave=valor;chave=valor
    public static Parser<java.util.Map<String,String>> keyValue(char pairSep, char kvSep) {
        return in -> {
            var map = new java.util.LinkedHashMap<String,String>();
            for (String pair: in.split(java.util.regex.Pattern.quote(String.valueOf(pairSep)))) {
                var idx = pair.indexOf(kvSep);
                if (idx < 0) return ParseResult.err(ParseError.of("Missing '=' in pair", 0, pair, "Ex.: key=value", null));
                map.put(pair.substring(0,idx).trim(), pair.substring(idx+1).trim());
            }
            return ParseResult.ok(java.util.Collections.unmodifiableMap(map));
        };
    }
}

