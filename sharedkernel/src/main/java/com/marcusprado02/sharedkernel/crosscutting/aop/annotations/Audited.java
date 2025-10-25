package com.marcusprado02.sharedkernel.crosscutting.aop.annotations;

import java.lang.annotation.*;

@Inherited @Retention(RetentionPolicy.RUNTIME) @Target({ElementType.METHOD, ElementType.TYPE})
public @interface Audited {
    String action() default "";
    boolean includeArgs() default false;     // cuidado com PII
    boolean sample() default true;           // amostragem
}