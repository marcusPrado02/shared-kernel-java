package com.marcusprado02.sharedkernel.cqrs.command.security;

import java.lang.annotation.*;

@Documented @Target({ElementType.TYPE}) @Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission { String[] value(); }
