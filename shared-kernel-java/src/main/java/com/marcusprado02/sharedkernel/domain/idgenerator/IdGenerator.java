package com.marcusprado02.sharedkernel.domain.idgenerator;

import com.marcusprado02.sharedkernel.domain.exception.IdGenerationException;

/**
 * Gera um identificador único de tipo T.
 */
public interface IdGenerator<T> {
    /**
     * @return um novo identificador único
     * @throws IdGenerationException se a geração falhar
     */
    T generate() throws IdGenerationException;
}
