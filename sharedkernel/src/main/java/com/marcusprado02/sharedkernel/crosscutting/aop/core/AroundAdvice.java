package com.marcusprado02.sharedkernel.crosscutting.aop.core;

@FunctionalInterface
public interface AroundAdvice {
    Object invoke(Invocation invocation) throws Throwable;
}