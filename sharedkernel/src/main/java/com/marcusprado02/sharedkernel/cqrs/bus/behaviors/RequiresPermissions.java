package com.marcusprado02.sharedkernel.cqrs.bus.behaviors;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RequiresPermissions { String[] value(); }
