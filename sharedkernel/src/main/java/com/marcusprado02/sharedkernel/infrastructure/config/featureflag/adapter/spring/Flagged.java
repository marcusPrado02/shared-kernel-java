package com.marcusprado02.sharedkernel.infrastructure.config.featureflag.adapter.spring;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Flagged {
  String key();
  String fallbackMethod() default ""; // opcional
}