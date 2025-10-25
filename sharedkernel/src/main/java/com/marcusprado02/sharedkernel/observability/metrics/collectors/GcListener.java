package com.marcusprado02.sharedkernel.observability.metrics.collectors;

public interface GcListener {
    void onGc(GcEvent evt, GcSnapshot afterUpdate);
}
