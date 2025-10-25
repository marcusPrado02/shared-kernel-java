package com.marcusprado02.sharedkernel.observability.logging;

import java.util.Map;

public interface LogEnricher {
    /** Enriquecer campos e aplicar políticas (redação, normalização, limites). Não deve lançar. */
    Map<String,Object> enrich(LogEvent event, LogContext ctx);
}
