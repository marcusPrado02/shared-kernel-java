package com.marcusprado02.sharedkernel.observability.metrics.annotation;

import java.lang.annotation.*;

@Documented @Retention(RetentionPolicy.RUNTIME) @Target({ElementType.METHOD, ElementType.TYPE})
public @interface Timed {
    String namespace() default "app";
    String name();
    String[] tags() default {}; // "key:value"
    boolean histogram() default true;
}
