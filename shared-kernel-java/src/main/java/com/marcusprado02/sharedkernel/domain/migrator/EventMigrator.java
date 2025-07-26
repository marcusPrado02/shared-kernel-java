package com.marcusprado02.sharedkernel.domain.migrator;

import com.marcusprado02.sharedkernel.domain.event.DomainEvent;

/**
 * Migra um evento leg legacy para a sua forma atual.
 *
 * @param <E> tipo de DomainEvent
 */
public interface EventMigrator<E extends DomainEvent> {
    /**
     * Retorna uma nova instância do evento, migrada para a estrutura mais recente. Se não houver
     * mudança, pode retornar o próprio objeto.
     */
    E migrate(E oldEvent);
}
