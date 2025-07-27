package com.marcusprado02.sharedkernel.domain.aggregateroot;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;
import com.marcusprado02.sharedkernel.domain.entity.BaseEntity;
import com.marcusprado02.sharedkernel.domain.event.DomainEvent;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Base para todos os Aggregate Roots: herda audit e id de BaseEntity, implementa Domain Events e
 * cleanup automático.
 */
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
public abstract class AbstractAggregateRoot<ID> extends BaseEntity implements AggregateRoot<ID> {

    /** Eventos acumulados a serem publicados */
    @Transient
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    /** Adiciona um evento de domínio */
    protected void addDomainEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    /** Retorna todos os eventos pendentes */
    @DomainEvents
    public Collection<DomainEvent> domainEvents() {
        return domainEvents;
    }

    /** Limpa eventos após publicação */
    @AfterDomainEventPublication
    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
