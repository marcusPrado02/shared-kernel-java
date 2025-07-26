package com.marcusprado02.sharedkernel.domain.service;


import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import com.marcusprado02.sharedkernel.domain.aggregateroot.AggregateRoot;
import com.marcusprado02.sharedkernel.domain.event.DomainEventPublisher;
import com.marcusprado02.sharedkernel.domain.repository.BaseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

/**
 * Base genérica para Domain Services.
 *
 * <T> Agregado raiz <ID> Tipo da chave primária <R> Porta de repositório
 */
@Slf4j
@Transactional // garante atomicidade (propagation = REQUIRED por padrão)
public abstract class AbstractDomainService<T extends AggregateRoot<ID>, ID, R extends BaseRepository<T, ID>>
        implements DomainService {

    protected final R repository;
    protected final DomainEventPublisher publisher;
    protected final Clock clock; // para testabilidade de datas/horas

    protected AbstractDomainService(R repository, DomainEventPublisher publisher, Clock clock) {
        this.repository = repository;
        this.publisher = publisher;
        this.clock = clock;
    }

    /* ==== API utilitária / “template methods” ============================ */

    protected T getOrFail(ID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entidade não encontrada: " + id));
    }

    protected Optional<T> find(ID id) {
        return repository.findById(id);
    }

    protected T persist(T aggregate) {
        beforeSave(aggregate);
        T saved = repository.save(aggregate);
        afterSave(saved);
        return saved;
    }

    protected void remove(ID id) {
        if (!repository.existsById(id))
            throw new EntityNotFoundException("Entidade inexistente: " + id);
        repository.deleteById(id);
        log.info("Entidade removida: {}", id);
    }

    /* ==== Hooks de extensão ============================================= */

    /** Validações de domínio, auditoria, etc. */
    protected void beforeSave(T aggregate) { /* default no-op */ }

    /** Publica eventos, métricas, logs estruturados */
    protected void afterSave(T aggregate) {
        aggregate.getDomainEvents().forEach(publisher::publish);
        aggregate.clearDomainEvents();
    }

    /* ==== Conveniências ================================================== */

    protected LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
