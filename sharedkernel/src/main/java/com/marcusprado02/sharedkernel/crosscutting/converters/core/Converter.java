package com.marcusprado02.sharedkernel.crosscutting.converters.core;

@FunctionalInterface
public interface Converter<S,T> {
    T convert(S source) throws ConversionException;
}

