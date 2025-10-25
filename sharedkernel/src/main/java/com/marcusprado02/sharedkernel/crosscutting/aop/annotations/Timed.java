package com.marcusprado02.sharedkernel.crosscutting.aop.annotations;

import java.lang.annotation.*;

@Inherited @Retention(RetentionPolicy.RUNTIME) @Target({ElementType.METHOD, ElementType.TYPE})
public @interface Timed {
    String metric() default "method.duration";
    boolean highCardinalityTags() default false;
}
