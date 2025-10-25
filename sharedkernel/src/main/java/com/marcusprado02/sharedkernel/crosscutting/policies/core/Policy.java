package com.marcusprado02.sharedkernel.crosscutting.policies.core;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;

@Inherited 
@Retention(RetentionPolicy.RUNTIME) 
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Policy {
    String action();                  // ex.: "order:view"
    String resource() default "";     // opcional; pode vir de argumento
    String resourceIdParam() default ""; // nome do parâmetro que contém ID
    String[] obligations() default {};    // chaves esperadas (para aplicar pós-decisão)
}