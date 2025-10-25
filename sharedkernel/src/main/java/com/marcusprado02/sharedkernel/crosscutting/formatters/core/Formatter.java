package com.marcusprado02.sharedkernel.crosscutting.formatters.core;

@FunctionalInterface
public interface Formatter<T> {
    String format(T value) throws FormatException;
}

