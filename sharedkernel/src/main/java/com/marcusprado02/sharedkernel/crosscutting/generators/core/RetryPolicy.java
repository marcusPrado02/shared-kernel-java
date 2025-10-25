package com.marcusprado02.sharedkernel.crosscutting.generators.core;

// Policies
interface RetryPolicy {
    <T> T execute(CheckedSupplier<T> supplier) throws GenerationException;
    @FunctionalInterface interface CheckedSupplier<T> { T get() throws Exception; }
}

