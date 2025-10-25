package com.marcusprado02.sharedkernel.crosscutting.sanitize.core;

public class SanitizationException extends RuntimeException {
    public SanitizationException(String m){ super(m); }
    public SanitizationException(String m, Throwable c){ super(m,c); }
}

