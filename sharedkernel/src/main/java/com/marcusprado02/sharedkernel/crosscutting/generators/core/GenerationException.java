package com.marcusprado02.sharedkernel.crosscutting.generators.core;

public class GenerationException extends RuntimeException {
    public GenerationException(String msg){super(msg);}
    public GenerationException(String msg, Throwable cause){super(msg,cause);}
}
