package com.marcusprado02.sharedkernel.crosscutting.converters.core;

public interface BidiConverter<A,B> extends Converter<A,B> {
    Converter<B,A> inverse();
}
