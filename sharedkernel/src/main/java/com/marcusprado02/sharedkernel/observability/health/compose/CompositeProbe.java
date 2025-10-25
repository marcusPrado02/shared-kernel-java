package com.marcusprado02.sharedkernel.observability.health.compose;

import java.util.*;

import com.marcusprado02.sharedkernel.observability.health.*;

public final class CompositeProbe implements ProbeCheck {
    public enum Mode { AND, OR_WEIGHTED }
    private final List<ProbeCheck> checks;
    private final Mode mode;
    private final Map<String,Integer> weight; // criticidade (maior = mais crítico)
    private final String name;

    public CompositeProbe(String name, Mode mode, List<ProbeCheck> checks, Map<String,Integer> weight) {
        this.name=name; this.mode=mode; this.checks=List.copyOf(checks); this.weight = weight==null?Map.of():Map.copyOf(weight);
    }

    @Override public ProbeResult check() {
        var results = new ArrayList<ProbeResult>();
        for (var c: checks) results.add(c.check());

        if (mode==Mode.AND) {
            var worst = results.stream().map(ProbeResult::status).max(Comparator.comparingInt(CompositeProbe::rank)).orElse(Status.UP);
            return new ProbeResult(worst, "AND_aggregate", Map.of("children", results), null);
        } else {
            // OR_WEIGHTED: se qualquer crítico (peso >= 10) DOWN => DOWN; se maioria ponderada UP => UP; senão DEGRADED
            int totalW=0, upW=0, downW=0;
            for (var r: results) {
                int w = weight.getOrDefault(r.reason(), 1); // ou use name()
                totalW += w;
                if (r.status()==Status.UP) upW += w;
                else if (r.status()==Status.DOWN) downW += w;
            }
            if (downW >= 10) return new ProbeResult(Status.DOWN, "critical_down", Map.of("children", results), null);
            if (upW*2 >= totalW) return new ProbeResult(Status.UP, "weighted_ok", Map.of("children", results), null);
            return new ProbeResult(Status.DEGRADED, "weighted_degraded", Map.of("children", results), null);
        }
    }
    private static int rank(Status s){ return switch (s){ case UP->0; case DEGRADED->1; case DOWN->2; }; }
    @Override public String name() { return name; }
}

