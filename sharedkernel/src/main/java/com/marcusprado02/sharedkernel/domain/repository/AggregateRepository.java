package com.marcusprado02.sharedkernel.domain.repository;


import java.util.List;
import java.util.Optional;

import com.marcusprado02.sharedkernel.domain.model.base.Identifier;
import com.marcusprado02.sharedkernel.domain.repository.errors.NotFoundException;

public interface AggregateRepository<A, ID extends Identifier> extends Repository {

    /** Cria/atualiza aplicando optimistic locking; retorna o agregado atual persistido. */
    A save(A aggregate);

    Optional<A> findById(ID id);

    /** Erga NotFoundException se não existir (evita Optional no app layer). */
    default A getById(ID id) {
        return findById(id).orElseThrow(() -> new NotFoundException(
            "Aggregate not found: " + id.asString()));
    }

    boolean existsById(ID id);

    /** Soft delete por padrão (se o agregado suportar); adapter decide política. */
    void delete(ID id);

    /** Consultas por Specification */
    List<A> findAll(Specification<A> spec, List<Sort> sort);

    PageResult<A> findAll(Specification<A> spec, PageRequest pageRequest);
}
