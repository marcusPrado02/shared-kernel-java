package com.marcusprado02.sharedkernel.observability.tracing.samplers;

public interface EventSampler {
    Result shouldSample(EventAttributes e);

    final class Result {
        public final SampleDecision decision;
        public final SampleReason reason;
        public final double score;       // utilitário (p.ex. prob usada) 0..1
        public final double ttlSeconds;  // dica p/ collector tail-based
        public Result(SampleDecision d, SampleReason r, double s, double ttl){ decision=d; reason=r; score=s; ttlSeconds=ttl; }
        public static Result drop(){ return new Result(SampleDecision.DROP, SampleReason.RULE, 0, 0); }
        public static Result keep(SampleReason r){ return new Result(SampleDecision.KEEP, r, 1, 0); }
        public static Result defer(double ttlSec){ return new Result(SampleDecision.DEFER, SampleReason.TAIL_HINT, 0.5, ttlSec); }
    }
}