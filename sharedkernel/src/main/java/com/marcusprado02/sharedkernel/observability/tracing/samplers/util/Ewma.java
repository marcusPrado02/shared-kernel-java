package com.marcusprado02.sharedkernel.observability.tracing.samplers.util;

public final class Ewma {
    private final double alpha; // 0..1 (ex.: 0.2)
    private double value; private boolean init;

    public Ewma(double alpha){ this.alpha = Math.max(0.01, Math.min(1.0, alpha)); }
    public double add(double x){ value = init? (alpha*x + (1-alpha)*value) : x; init=true; return value; }
    public double value(){ return init? value : 0.0; }
}
