package com.marcusprado02.sharedkernel.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.marcusprado02.sharedkernel.contracts.api.SearchQuery;

import java.util.Optional;

public interface CrudApplicationService<RES, CREATE, UPDATE, ID> {
    RES create(CREATE cmd);
    Optional<RES> findById(ID id);
    RES update(ID id, UPDATE cmd);
    void delete(ID id);

    /** Busca paginada/ordenada com filtros livres; implementação escolhe como interpretá-los. */
    Page<RES> search(SearchQuery query, Pageable pageable);

    /** Versão lógica atual do recurso (para ETag/If-Match/If-None-Match). Pode ser hash, version, updatedAt, etc. */
    Optional<String> versionOf(ID id);
}
