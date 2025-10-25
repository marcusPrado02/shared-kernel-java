package com.marcusprado02.sharedkernel.cqrs.query;


import java.util.List;

/** Ordenação segura (campo + direção) com whitelist. */
public record Sort(String field, Direction direction) {
    public enum Direction { ASC, DESC }
    public static boolean isAllowed(String field, List<String> whitelist){ return whitelist.contains(field); }
}
