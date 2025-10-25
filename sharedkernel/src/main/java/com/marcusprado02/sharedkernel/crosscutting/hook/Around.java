package com.marcusprado02.sharedkernel.crosscutting.hook;

@FunctionalInterface public interface Around<I,O> {
    O apply(I input, HookContext ctx, Proceed<I,O> next) throws Exception;
}
