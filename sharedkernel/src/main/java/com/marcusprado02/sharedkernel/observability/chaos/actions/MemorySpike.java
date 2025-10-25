package com.marcusprado02.sharedkernel.observability.chaos.actions;

import com.marcusprado02.sharedkernel.observability.chaos.*;

public final class MemorySpike implements ChaosAction {
    private final int megabytes;
    public MemorySpike(int mb){ this.megabytes = Math.max(1, Math.min(256, mb)); } // guardrail
    @Override public void apply(ChaosContext ctx) {
        byte[][] junk = new byte[megabytes][];
        for (int i=0;i<megabytes;i++){ junk[i] = new byte[1024*1024]; }
        // Deixa para GC limpar após retorno (não reter referência em campo)
    }
    @Override public String name(){ return "memory"; }
}
