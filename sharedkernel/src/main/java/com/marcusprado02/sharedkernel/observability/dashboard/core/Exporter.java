package com.marcusprado02.sharedkernel.observability.dashboard.core;

import com.marcusprado02.sharedkernel.observability.dashboard.model.Dashboard;
import com.marcusprado02.sharedkernel.observability.dashboard.spi.DashboardPublisher;
import com.marcusprado02.sharedkernel.observability.dashboard.spi.DashboardRenderer;

public interface Exporter {
    /** Renderiza e exporta (upsert) — ou salva em arquivo se publisher for null. */
    Result export(Dashboard d, DashboardRenderer r, DashboardPublisher p, java.nio.file.Path fileOut) throws Exception;

    record Result(String uid, boolean changed, java.nio.file.Path written) {}
}