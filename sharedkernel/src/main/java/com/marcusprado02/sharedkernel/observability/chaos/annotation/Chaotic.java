package com.marcusprado02.sharedkernel.observability.chaos.annotation;

import java.lang.annotation.*;

@Documented @Retention(RetentionPolicy.RUNTIME) @Target({ElementType.METHOD, ElementType.TYPE})
public @interface Chaotic {
    String route() default "";    // nome lógico do ponto (ex.: service:method)
    String method() default "INTERNAL";
}