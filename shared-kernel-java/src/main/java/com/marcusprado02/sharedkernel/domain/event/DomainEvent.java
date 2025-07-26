package com.marcusprado02.sharedkernel.domain.event;


import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Contrato para eventos de domínio compatíveis com Event Sourcing.
 */
public interface DomainEvent extends Serializable {

    /**
     * ID único do evento (idealmente um UUID).
     */
    UUID getEventId();

    /**
     * Data/hora exata da ocorrência (idealmente UTC).
     */
    Instant getOccurredOn();


    /** Tipo do agregado associado (opcional para frameworks) */
    String getAggregateType();

    /** Identificador do agregado associado */
    String getAggregateId();


    /**
     * Tipo lógico do evento para roteamento e persistência.
     */
    default String getEventType() {
        return this.getClass().getSimpleName();
    }
}
