package com.marcusprado02.sharedkernel.crosscutting.hook;

/** Callback transformacional (produz valor). */
@FunctionalInterface
public interface Callback<I, O> {
    O call(I input, HookContext ctx) throws Exception;
}
