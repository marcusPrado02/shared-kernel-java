package com.marcusprado02.sharedkernel.domain.event;


import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.ToString;

/**
 * Implementação base de um evento de domínio com suporte a Event Sourcing.
 */
@Getter
@ToString
public abstract class AbstractDomainEvent implements DomainEvent {

    private final UUID eventId;
    private final Instant occurredOn;
    private final String aggregateType;
    private final String aggregateId;

    /**
     * Construtor protegido para eventos de domínio.
     *
     * @param aggregateType nome da classe do agregado
     * @param aggregateId identificador do agregado (pode ser UUID ou String)
     */
    protected AbstractDomainEvent(String aggregateType, String aggregateId) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
    }
}
