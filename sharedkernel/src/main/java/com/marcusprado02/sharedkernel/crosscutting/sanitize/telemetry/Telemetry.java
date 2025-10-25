package com.marcusprado02.sharedkernel.crosscutting.sanitize.telemetry;


public interface Telemetry {
    void count(String name, double v);
    <R> R time(String name, java.util.concurrent.Callable<R> c) throws Exception;

    Telemetry NOOP = new Telemetry() {
        public void count(String n, double v) {}
        public <R> R time(String n, java.util.concurrent.Callable<R> c) throws Exception { return c.call(); }
    };
}