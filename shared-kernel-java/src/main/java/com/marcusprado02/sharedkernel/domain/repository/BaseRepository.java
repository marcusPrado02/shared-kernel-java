package com.marcusprado02.sharedkernel.domain.repository;


import java.util.List;
import java.util.Optional;

/**
 * Contrato genérico base para repositórios de persistência, com suporte a bancos SQL e NoSQL.
 *
 * @param <T> Tipo da entidade
 * @param <ID> Tipo do identificador primário
 */
public interface BaseRepository<T, ID> {

    /**
     * Retorna uma entidade pelo seu ID.
     *
     * @param id identificador primário
     * @return entidade encontrada (ou vazia)
     */
    Optional<T> findById(ID id);

    /**
     * Verifica se existe uma entidade com o ID fornecido.
     *
     * @param id identificador primário
     * @return true se existir
     */
    boolean existsById(ID id);

    /**
     * Retorna todas as entidades.
     *
     * @return lista de todas as entidades
     */
    List<T> findAll();

    /**
     * Persiste ou atualiza uma entidade.
     *
     * @param entity entidade a ser salva
     * @return entidade salva
     */
    T save(T entity);

    /**
     * Persiste ou atualiza múltiplas entidades.
     *
     * @param entities lista de entidades
     * @return lista de entidades salvas
     */
    List<T> saveAll(Iterable<T> entities);

    /**
     * Remove a entidade com o ID especificado.
     *
     * @param id identificador primário
     */
    void deleteById(ID id);

    /**
     * Remove a entidade fornecida.
     *
     * @param entity entidade a ser removida
     */
    void delete(T entity);

    /**
     * Remove todas as entidades fornecidas.
     *
     * @param entities entidades a serem removidas
     */
    void deleteAll(Iterable<T> entities);

    /**
     * Remove todas as entidades da coleção.
     */
    void deleteAll();

    /**
     * Retorna a contagem total de entidades.
     *
     * @return número de registros
     */
    long count();
}

