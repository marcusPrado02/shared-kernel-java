package com.marcusprado02.sharedkernel.observability.dashboard.spi;

import com.marcusprado02.sharedkernel.observability.dashboard.model.Dashboard;

public interface DashboardPublisher {
    /** Upsert idempotente (cria/atualiza) no provedor. Retorna UID/ID final. */
    String upsert(Dashboard d, byte[] payload, String contentType) throws Exception;

    /** Obtém payload atual para detecção de drift (opcional). */
    byte[] fetchCurrent(String uid) throws Exception;

    String backend(); // "grafana", "kibana"
}
