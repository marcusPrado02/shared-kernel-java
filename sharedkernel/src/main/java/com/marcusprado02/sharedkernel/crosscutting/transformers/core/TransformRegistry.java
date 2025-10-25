package com.marcusprado02.sharedkernel.crosscutting.transformers.core;

public interface TransformRegistry {
    <I,O> void register(String name, String version, Class<I> in, Class<O> out, TransformFunction<I,O> fn);
    <I,O> TransformFunction<I,O> resolve(String name, String version, Class<I> in, Class<O> out);
}
