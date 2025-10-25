package com.marcusprado02.sharedkernel.domain.exception.model;


public record ErrorCode(String namespace, String name, int version) {
    public String fqn() { return namespace + "." + name + ".v" + version; }
    public static ErrorCode of(String ns, String name, int v) { return new ErrorCode(ns, name, v); }
}
