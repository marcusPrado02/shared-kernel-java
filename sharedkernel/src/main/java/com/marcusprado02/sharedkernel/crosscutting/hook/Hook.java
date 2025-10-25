package com.marcusprado02.sharedkernel.crosscutting.hook;


/** Hook genérico (efeito colateral). */
@FunctionalInterface
public interface Hook<E> {
    void handle(E event, HookContext ctx) throws Exception;
}
