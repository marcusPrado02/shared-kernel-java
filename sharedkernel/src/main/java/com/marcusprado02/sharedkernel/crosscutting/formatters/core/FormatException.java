package com.marcusprado02.sharedkernel.crosscutting.formatters.core;

public class FormatException extends RuntimeException {
    public FormatException(String msg, Throwable cause) { super(msg, cause); }
    public FormatException(String msg) { super(msg); }
}
