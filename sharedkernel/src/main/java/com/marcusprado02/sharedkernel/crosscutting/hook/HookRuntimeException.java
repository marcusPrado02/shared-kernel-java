package com.marcusprado02.sharedkernel.crosscutting.hook;

public final class HookRuntimeException extends RuntimeException {
    public HookRuntimeException(String m){ super(m); }
    public HookRuntimeException(String m, Throwable c){ super(m,c); }
}
