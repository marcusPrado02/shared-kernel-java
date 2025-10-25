package com.marcusprado02.sharedkernel.domain.model.base;

import java.io.Serializable;

/** Marcador para IDs fortes (Value Object). */
public interface Identifier extends Serializable {
    String asString(); // representação estável (log/contratos)
}
