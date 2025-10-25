package com.marcusprado02.sharedkernel.crosscutting.transformers.core;

@FunctionalInterface
public interface TransformFunction<I, O> {
    TransformResult<O> apply(I input, TransformContext ctx) throws Exception;
}