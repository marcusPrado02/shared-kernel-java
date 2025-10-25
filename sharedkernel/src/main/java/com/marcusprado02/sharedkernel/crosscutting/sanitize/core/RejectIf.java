package com.marcusprado02.sharedkernel.crosscutting.sanitize.core;

@FunctionalInterface public interface RejectIf<T> { void check(T in, SanitizationContext ctx) throws SanitizationException; }


