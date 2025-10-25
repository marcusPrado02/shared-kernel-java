package com.marcusprado02.sharedkernel.infrastructure.cdn;

public record Target(
        Dimension dimension,
        String value // ex: "/assets/*", "product:1234", "static.example.com"
) {
    public static Target path(String p){ return new Target(Dimension.PATH, p); }
    public static Target key(String k){ return new Target(Dimension.SURROGATE_KEY, k); }
    public static Target host(String h){ return new Target(Dimension.HOST, h); }
    public static Target purgeAll(){ return new Target(Dimension.ALL, "*"); }
}
