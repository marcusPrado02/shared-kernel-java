package com.marcusprado02.sharedkernel.domain.events.model;

public record EventType(String namespace, String name) {
    public String fqn() { return namespace + "." + name; }
}

