package com.marcusprado02.sharedkernel.infrastructure.persistence.document;

import com.marcusprado02.sharedkernel.infrastructure.persistence.sql.exceptions.NotFoundException;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.transaction.annotation.Transactional;
import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.Criteria;
import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.PageRequest;
import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.Sort;

import com.marcusprado02.sharedkernel.domain.model.base.TenantScoped;
import com.marcusprado02.sharedkernel.domain.model.base.Versioned;
import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.Filter;
import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.Page;
import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.SeekKey;
import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.SeekPage;
import com.marcusprado02.sharedkernel.infrastructure.tenancy.TenantProvider;

import java.time.Instant;
import java.util.*;

/**
 * Repositório base para MongoDB/DocumentDB com multi-tenant, soft-delete opcional,
 * paginação offset e keyset, e mapeamento por Template.
 *
 * Requisitos:
 * - Spring Data MongoDB no classpath.
 * - Java 17+.
 * - Suas classes de criteria/exceções conforme imports acima.
 */
public abstract class BaseDocumentRepository<E extends Versioned & TenantScoped, ID>
    implements DocumentRepository<E, ID> {

  protected final MongoTemplate mongo;
  protected final Class<E> entityClass;
  protected final String collection;
  protected final TenantProvider tenantProvider;
  protected final boolean softDelete;

  protected BaseDocumentRepository(MongoTemplate mongo,
                                   Class<E> entityClass,
                                   String collection,
                                   TenantProvider tenantProvider,
                                   boolean softDelete) {
    this.mongo = mongo;
    this.entityClass = entityClass;
    this.collection = collection;
    this.tenantProvider = tenantProvider;
    this.softDelete = softDelete;
  }

  protected String tenant() { return tenantProvider.tenantId(); }

  // ------------------ Guards de escopo ------------------
  protected org.springframework.data.mongodb.core.query.Criteria tenantGuard() {
    return org.springframework.data.mongodb.core.query.Criteria.where("tenantId").is(tenant());
  }

  protected org.springframework.data.mongodb.core.query.Criteria notDeletedGuard() {
    return org.springframework.data.mongodb.core.query.Criteria.where("deletedAt").is(null);
  }

  // ------------------ Tradução da sua DSL -> Query do Spring ------------------
  protected Query toMongoQuery(Criteria c) {
    List<org.springframework.data.mongodb.core.query.Criteria> ands = new ArrayList<>();
    ands.add(tenantGuard());
    if (softDelete) ands.add(notDeletedGuard());

    for (Filter f : c.filters()) {
      String field = f.field();
      Object v = f.value();
      switch (f.op()) {
        case EQ -> ands.add(org.springframework.data.mongodb.core.query.Criteria.where(field).is(v));
        case NE -> ands.add(org.springframework.data.mongodb.core.query.Criteria.where(field).ne(v));
        case GT -> ands.add(org.springframework.data.mongodb.core.query.Criteria.where(field).gt(v));
        case GTE-> ands.add(org.springframework.data.mongodb.core.query.Criteria.where(field).gte(v));
        case LT -> ands.add(org.springframework.data.mongodb.core.query.Criteria.where(field).lt(v));
        case LTE-> ands.add(org.springframework.data.mongodb.core.query.Criteria.where(field).lte(v));
        case LIKE -> ands.add(org.springframework.data.mongodb.core.query.Criteria.where(field).regex(v.toString(), "i"));
        case IN -> ands.add(org.springframework.data.mongodb.core.query.Criteria.where(field).in((Collection<?>) v));
        case BETWEEN -> {
          List<?> list = (List<?>) v;
          ands.add(org.springframework.data.mongodb.core.query.Criteria.where(field).gte(list.get(0)).lte(list.get(1)));
        }
        case IS_NULL  -> ands.add(org.springframework.data.mongodb.core.query.Criteria.where(field).is(null));
        case NOT_NULL -> ands.add(org.springframework.data.mongodb.core.query.Criteria.where(field).ne(null));
      }
    }

    org.springframework.data.mongodb.core.query.Criteria criteria = new org.springframework.data.mongodb.core.query.Criteria().andOperator(ands);

    Query q = new Query(criteria);

    c.sort().ifPresent(s ->
        q.with(org.springframework.data.domain.Sort.by(s.asc() ? Direction.ASC : Direction.DESC, s.field()))
    );

    return q;
  
  }

  // ------------------ CRUD ------------------

  @Override
  public Optional<E> findById(ID id) {
    Query q = new Query(org.springframework.data.mongodb.core.query.Criteria.where("_id").is(id))
        .addCriteria(tenantGuard());
    if (softDelete) q.addCriteria(notDeletedGuard());
    return Optional.ofNullable(mongo.findOne(q, entityClass, collection));
  }

  @Override
  public E getById(ID id) {
    return findById(id).orElseThrow(() -> new NotFoundException(collection + " id=" + id));
  }

  @Override
  public List<E> findAll(Criteria criteria) {
    return mongo.find(toMongoQuery(criteria), entityClass, collection);
  }

  @Override
  public Page<E> findPage(Criteria c, PageRequest page) {
    Query q = toMongoQuery(c);
    long total = mongo.count(q, collection);
    q.skip((long) page.page() * page.size());
    q.limit(page.size());
    List<E> content = mongo.find(q, entityClass, collection);
    return new Page<>(content, total, page.page(), page.size());
  }

  @Override
  public SeekPage<E> findPageBySeek(Criteria c, Sort sort, Optional<SeekKey> after, int limit) {
    Query q = toMongoQuery(c);

    after.ifPresent(sk -> {
      Object v = sk.values()[0];
      // Para sort por campo ≠ _id, crie índice composto (tenantId, campo).
      org.springframework.data.mongodb.core.query.Criteria cmp =
          sort.asc()
              ? new org.springframework.data.mongodb.core.query.Criteria(sort.field()).gt(v)
              : new org.springframework.data.mongodb.core.query.Criteria(sort.field()).lt(v);
      q.addCriteria(cmp);
    });

    q.limit(limit);

    List<E> rows = mongo.find(q, entityClass, collection);

    Optional<SeekKey> next =
        rows.isEmpty()
            ? Optional.empty()
            : Optional.of(new SeekKey(new Object[] { extractSortValue(rows.get(rows.size() - 1), sort.field()) }));

    return new SeekPage<>(rows, next);
  }

  protected Object extractSortValue(E e, String field) {
    try {
      var m = e.getClass().getMethod("get" + Character.toUpperCase(field.charAt(0)) + field.substring(1));
      return m.invoke(e);
    } catch (Exception ex) {
      throw new IllegalArgumentException("Não foi possível extrair sort field " + field, ex);
    }
  }

  @Override
  public boolean exists(Criteria c) {
    return mongo.exists(toMongoQuery(c), entityClass, collection);
  }

  @Override
  public long count(Criteria c) {
    return mongo.count(toMongoQuery(c), entityClass, collection);
  }

  @Override
  public E insert(E e) {
    ensureTenantAndTimestamps(e, true);
    return mongo.insert(e, collection);
  }

  @Override
  public E update(E e) {
    ensureTenantAndTimestamps(e, false);
    // @Version no documento garante optimistic lock; save faz replace por _id+version
    return mongo.save(e, collection);
  }

  @Override
  public E upsert(E e) {
    ensureTenantAndTimestamps(e, false);
    return mongo.save(e, collection);
  }

  @Override
  public void deleteById(ID id) {
    if (softDelete) {
      Update update = new Update().set("deletedAt", Instant.now());
      mongo.updateFirst(
          new Query(org.springframework.data.mongodb.core.query.Criteria.where("_id").is(id))
              .addCriteria(tenantGuard()),
          update,
          collection
      );
    } else {
      mongo.remove(
          new Query(org.springframework.data.mongodb.core.query.Criteria.where("_id").is(id))
              .addCriteria(tenantGuard()),
          collection
      );
    }
  }

  @Override
  public void delete(Criteria c) {
    if (softDelete) {
      Query q = toMongoQuery(c);
      mongo.updateMulti(q, new Update().set("deletedAt", Instant.now()), collection);
    } else {
      mongo.remove(toMongoQuery(c), collection);
    }
  }

  @Override
  public int[] batchInsert(List<E> entities) {
    entities.forEach(e -> ensureTenantAndTimestamps(e, true));
    mongo.insert(entities, collection);
    return entities.stream().mapToInt(x -> 1).toArray();
  }

  @Override
  public int[] batchUpdate(List<E> entities) {
    entities.forEach(e -> ensureTenantAndTimestamps(e, false));
    entities.forEach(e -> mongo.save(e, collection));
    return entities.stream().mapToInt(x -> 1).toArray();
  }

  // Em Mongo, transações multi-document exigem cluster com suporte; em DocumentDB podem não existir.
  @Override
  @Transactional // no-op aqui; use ClientSession se seu cluster suportar transações
  public void withTransaction(Runnable work) {
    work.run();
  }

  // ------------------ util ------------------

  private void ensureTenantAndTimestamps(E e, boolean isCreate) {
    try {
      if (e.tenantId() == null || e.tenantId().isBlank()) {
        var m = e.getClass().getMethod("setTenantId", String.class);
        m.invoke(e, tenant());
      }
      Instant now = Instant.now();
      if (isCreate) {
        try {
          e.getClass().getMethod("setCreatedAt", Instant.class).invoke(e, now);
        } catch (NoSuchMethodException ignore) {}
      }
      try {
        e.getClass().getMethod("setUpdatedAt", Instant.class).invoke(e, now);
      } catch (NoSuchMethodException ignore) {}
    } catch (Exception ex) {
      throw new IllegalStateException("Falha ao preencher tenant/timestamps", ex);
    }
  }
}