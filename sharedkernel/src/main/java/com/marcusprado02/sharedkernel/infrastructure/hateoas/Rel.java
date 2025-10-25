package com.marcusprado02.sharedkernel.infrastructure.hateoas;

import java.util.Objects;

public record Rel(String value) {
    public static Rel of(String v){ return new Rel(Objects.requireNonNull(v)); }
}