package com.marcusprado02.sharedkernel.crosscutting.converters.core;

public final class ConversionException extends RuntimeException {
    public ConversionException(String msg) { super(msg); }
    public ConversionException(String msg, Throwable cause) { super(msg, cause); }
}