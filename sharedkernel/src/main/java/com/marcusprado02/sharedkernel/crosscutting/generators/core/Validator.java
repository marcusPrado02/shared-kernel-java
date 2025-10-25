package com.marcusprado02.sharedkernel.crosscutting.generators.core;

@FunctionalInterface public interface Validator<T> { void validate(T value, GenerationContext ctx) throws GenerationException; }


