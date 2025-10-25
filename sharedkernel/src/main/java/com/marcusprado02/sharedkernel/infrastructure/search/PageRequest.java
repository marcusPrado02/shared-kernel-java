package com.marcusprado02.sharedkernel.infrastructure.search;

public record PageRequest(int page, int size, String cursor) {
    public static PageRequest of(int page, int size) { return new PageRequest(page, size, null); }
    public static PageRequest cursor(String cursor, int size) { return new PageRequest(-1, size, cursor); }
}
