package com.marcusprado02.sharedkernel.crosscutting.hook;

import java.util.List;

/** Executor de callback com around-chain + hooks before/after/error. */
public final class CallbackExecutor {
    private final HookBus bus;
    private final List<Around<?,?>> arounds; // ordenadas por prioridade se quiser
    public CallbackExecutor(HookBus bus, List<Around<?,?>> arounds){ this.bus=bus; this.arounds=List.copyOf(arounds); }

    @SuppressWarnings("unchecked")
    public <I,O> O execute(String topic, Callback<I,O> cb, I in, HookContext base) throws Exception {
        HookContext ctx = HookContext.builder()
                .topic(topic).phase("invoke")
                .clock(base.clock()).cancellation(base.cancellation())
                .correlationId(base.correlationId().orElse(null))
                .build();
        // BEFORE
        bus.fire(topic, HookPhase.BEFORE, in, ctx);

        // AROUND chain
        Proceed<I,O> terminal = (input, c) -> cb.call(input, c);
        for (int i = arounds.size()-1; i>=0; i--) {
            Around<I,O> layer = (Around<I,O>) arounds.get(i);
            Proceed<I,O> next = terminal;
            terminal = (input, c) -> layer.apply(input, c, next);
        }

        try {
            O out = terminal.call(in, ctx);
            // AFTER (com saída)
            bus.fire(topic, HookPhase.AFTER, out, ctx);
            return out;
        } catch (Exception e) {
            // ERROR
            bus.fire(topic, HookPhase.ERROR, e, ctx);
            throw e;
        }
    }
}
