package com.marcusprado02.sharedkernel.crosscutting.sanitize.core;

@FunctionalInterface public interface Rule<T> { T apply(T in, SanitizationContext ctx); }

