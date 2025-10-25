package com.marcusprado02.sharedkernel.crosscutting.aop.annotations;


import java.lang.annotation.*;

@Inherited @Retention(RetentionPolicy.RUNTIME) @Target({ElementType.METHOD, ElementType.TYPE})
public @interface Retryable {
    int maxAttempts() default 3;
    long backoffMs() default 200;
    Class<? extends Throwable>[] retryOn() default {RuntimeException.class};
}
