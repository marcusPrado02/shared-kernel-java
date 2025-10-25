package com.marcusprado02.sharedkernel.observability.profiling;

public interface ProfilerAdapter {
    void start(ProfilingContext ctx, EvaluationResult why);
    void stop(ProfilingContext ctx, EvaluationResult why);
}
