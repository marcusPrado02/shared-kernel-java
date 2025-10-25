package com.marcusprado02.sharedkernel.crosscutting.generators.core;

@FunctionalInterface public interface Transform<I,O> { O apply(I in, GenerationContext ctx); }

