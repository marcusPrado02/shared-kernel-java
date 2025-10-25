package com.marcusprado02.sharedkernel.contracts.api;

import java.net.InetAddress;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/** Builder de cadeia com ordering estável e predicados. */
public final class FilterChainBuilder {
    private final List<FilterDef> defs = new ArrayList<>();
    public FilterChainBuilder add(FilterDef def){ defs.add(def); return this; }
    public ApiFilter build(EndpointHandler terminalHandler){
        defs.sort(Comparator.comparingInt(FilterDef::order));
        return new BuiltChain(List.copyOf(defs), terminalHandler);
    }
    private static final class BuiltChain implements ApiFilter {
        private final List<FilterDef> defs;
        private final EndpointHandler terminal;
        BuiltChain(List<FilterDef> defs, EndpointHandler terminal){ 
            this.defs = defs; 
            this.terminal = terminal; 
        }

        @Override public FilterResult apply(ApiExchange exchange, Chain chain) throws Exception {
            // O BuiltChain é sempre a raiz; delega ao index 0.
            return proceedFrom(0, exchange);
        }

        private FilterResult proceedFrom(int idx, ApiExchange ex) throws Exception {
            if (idx >= defs.size()) { // Terminal decorator: chama o handler final
                var resp = terminal.handle(ex);
                ex.setResponse(resp);
                return new FilterResult.Halt(ex);
            }
            var def = defs.get(idx);
            if (def.when().test(ex)) {
                return def.filter().apply(ex, nxt -> proceedFrom(idx+1, ex));
            } else {
                return proceedFrom(idx+1, ex);
            }
        }
    }
}
