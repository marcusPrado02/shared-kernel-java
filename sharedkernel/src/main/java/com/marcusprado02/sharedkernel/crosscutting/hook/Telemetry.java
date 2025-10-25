package com.marcusprado02.sharedkernel.crosscutting.hook;

import java.util.Map;

public interface Telemetry {
    void count(String name, double v, Map<String, String> tags);
    <T> T time(String name, java.util.concurrent.Callable<T> c, Map<String,String> tags) throws Exception;

    Telemetry NOOP = new Telemetry() {
        public void count(String n, double v, Map<String,String> t) {}
        public <T> T time(String n, java.util.concurrent.Callable<T> c, Map<String,String> t) throws Exception { return c.call(); }
    };
}
