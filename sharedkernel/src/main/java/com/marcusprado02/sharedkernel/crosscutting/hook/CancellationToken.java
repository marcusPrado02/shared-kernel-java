package com.marcusprado02.sharedkernel.crosscutting.hook;

public interface CancellationToken {
    boolean isCancelled();
    CancellationToken NONE = () -> false;
}

