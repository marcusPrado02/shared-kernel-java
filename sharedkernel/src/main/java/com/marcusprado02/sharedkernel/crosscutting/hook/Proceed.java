package com.marcusprado02.sharedkernel.crosscutting.hook;

@FunctionalInterface public interface Proceed<I,O> {
    O call(I input, HookContext ctx) throws Exception;
}
