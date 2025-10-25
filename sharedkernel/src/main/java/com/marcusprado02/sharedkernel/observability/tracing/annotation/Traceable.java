package com.marcusprado02.sharedkernel.observability.tracing.annotation;

import java.lang.annotation.*;

import com.marcusprado02.sharedkernel.observability.tracing.SpanKind;

@Documented @Retention(RetentionPolicy.RUNTIME) @Target({ElementType.METHOD, ElementType.TYPE})
public @interface Traceable {
    String name();
    SpanKind kind() default SpanKind.INTERNAL;
    String[] attributes() default {}; // "key:value"
    boolean captureExceptions() default true;
}
