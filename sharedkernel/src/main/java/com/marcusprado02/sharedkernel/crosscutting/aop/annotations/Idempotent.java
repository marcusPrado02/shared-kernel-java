package com.marcusprado02.sharedkernel.crosscutting.aop.annotations;

import java.lang.annotation.*;

import java.lang.annotation.*;
@Inherited @Retention(RetentionPolicy.RUNTIME) @Target({ElementType.METHOD, ElementType.TYPE})
public @interface Idempotent {
    String key() default "";                 // se vazio, gerar hash(args)
    long ttlSeconds() default 600;
}
