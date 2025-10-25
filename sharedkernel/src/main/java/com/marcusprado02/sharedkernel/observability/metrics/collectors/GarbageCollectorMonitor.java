package com.marcusprado02.sharedkernel.observability.metrics.collectors;


public interface GarbageCollectorMonitor extends AutoCloseable {
    void addListener(GcListener l);
    GcSnapshot current(); // estatísticas atuais (janelas)
    @Override void close();
}
