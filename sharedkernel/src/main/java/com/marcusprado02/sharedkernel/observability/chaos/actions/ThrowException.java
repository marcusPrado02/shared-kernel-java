package com.marcusprado02.sharedkernel.observability.chaos.actions;

import com.marcusprado02.sharedkernel.observability.chaos.*;

public final class ThrowException implements ChaosAction {
    private final RuntimeException ex;
    public ThrowException(String msg){ this.ex = new RuntimeException("[Chaos] " + msg); }
    @Override public void apply(ChaosContext ctx){ throw ex; }
    @Override public String name(){ return "exception"; }
}
