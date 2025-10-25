package com.marcusprado02.sharedkernel.domain.model.base;

import java.util.Objects;

/** Value Object para versionamento (incrementado na persistência). */
public record Version(long value) {
    public Version {
        if (value < 0) throw new IllegalArgumentException("version must be >= 0");
    }
    public Version next() { return new Version(value + 1); }
    public static Version zero() { return new Version(0L); }
}