package com.marcusprado02.sharedkernel.observability.chaos;

import java.util.*;

public interface ChaosEngine {
    /** Tenta aplicar caos para um contexto; retorna verdadeiro se injetou. */
    boolean maybeInject(ChaosContext ctx) throws Exception;

    /** Administração básica (list/start/stop). */
    List<ChaosPolicy> policies();
    void addPolicy(ChaosPolicy p);
    void removePolicy(String id);
    void clear();

    /** Kill switch global. */
    void setEnabled(boolean enabled);
    boolean isEnabled();
}
