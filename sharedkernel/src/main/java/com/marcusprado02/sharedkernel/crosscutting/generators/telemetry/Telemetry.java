package com.marcusprado02.sharedkernel.crosscutting.generators.telemetry;

public interface Telemetry {
    void count(String name, double value);
    <T> T time(String name, java.util.concurrent.Callable<T> c) throws Exception;

    Telemetry NOOP = new Telemetry() {
        public void count(String n, double v) {}
        public <T> T time(String n, java.util.concurrent.Callable<T> c) throws Exception { return c.call(); }
    };
}