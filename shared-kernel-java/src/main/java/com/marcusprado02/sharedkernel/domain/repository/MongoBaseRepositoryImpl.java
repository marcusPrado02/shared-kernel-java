package com.marcusprado02.sharedkernel.domain.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

public abstract class MongoBaseRepositoryImpl<T, ID> implements BaseRepository<T, ID> {

    protected final MongoTemplate mongoTemplate;
    private final Class<T> entityClass;

    protected MongoBaseRepositoryImpl(MongoTemplate mongoTemplate, Class<T> entityClass) {
        this.mongoTemplate = mongoTemplate;
        this.entityClass = entityClass;
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(mongoTemplate.findById(id, entityClass));
    }

    @Override
    public boolean existsById(ID id) {
        return findById(id).isPresent();
    }

    @Override
    public List<T> findAll() {
        return mongoTemplate.findAll(entityClass);
    }

    @Override
    public T save(T entity) {
        return mongoTemplate.save(entity);
    }

    @Override
    public List<T> saveAll(Iterable<T> entities) {
        List<T> result = new java.util.ArrayList<>();
        for (T entity : entities) {
            result.add(save(entity));
        }
        return result;
    }

    @Override
    public void deleteById(ID id) {
        findById(id).ifPresent(entity -> mongoTemplate.remove(entity));
    }

    @Override
    public void delete(T entity) {
        mongoTemplate.remove(entity);
    }

    @Override
    public void deleteAll(Iterable<T> entities) {
        for (T entity : entities) {
            delete(entity);
        }
    }

    @Override
    public void deleteAll() {
        mongoTemplate.remove(new Query(), entityClass);
    }

    @Override
    public long count() {
        return mongoTemplate.count(new Query(), entityClass);
    }
}
