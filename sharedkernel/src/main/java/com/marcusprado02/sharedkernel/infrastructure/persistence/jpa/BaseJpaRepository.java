package com.marcusprado02.sharedkernel.infrastructure.persistence.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;

import org.hibernate.Session;
import org.springframework.data.jpa.domain.Specification;

import com.marcusprado02.sharedkernel.domain.model.base.TenantScoped;
import com.marcusprado02.sharedkernel.domain.model.base.Versioned;
import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.Criteria;
import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.Page;
import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.PageRequest;
import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.SeekKey;
import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.SeekPage;
import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.Sort;
import com.marcusprado02.sharedkernel.infrastructure.persistence.sql.SqlRepository;
import com.marcusprado02.sharedkernel.infrastructure.persistence.sql.exceptions.NotFoundException;
import com.marcusprado02.sharedkernel.infrastructure.tenancy.TenantProvider;

@Transactional
public abstract class BaseJpaRepository<E extends Versioned & TenantScoped, ID>
    implements SqlRepository<E, ID> {

  @PersistenceContext
  protected EntityManager em;

  private final Class<E> entityClass;
  private final TenantProvider tenantProvider;

  protected BaseJpaRepository(Class<E> entityClass, TenantProvider tenantProvider) {
    this.entityClass = entityClass;
    this.tenantProvider = tenantProvider;
  }

  protected String currentTenant() { return tenantProvider.tenantId(); }

  @PostConstruct
  public void enableTenantFilter() {
    // hook; normalmente feito via interceptor/filter de request
  }

  @Override
  public Optional<E> findById(ID id) {
    E e = em.find(entityClass, id, LockModeType.NONE);
    return Optional.ofNullable(e);
  }

  @Override
  public E getById(ID id) {
    return findById(id).orElseThrow(() -> new NotFoundException(entityClass.getSimpleName() + " id=" + id));
  }

  @Override
  public List<E> findAll(Criteria criteria) {
    Specification<E> spec = Specs.toJpaSpecification(criteria);
    var cb = em.getCriteriaBuilder();
    var q = cb.createQuery(entityClass);
    var root = q.from(entityClass);

    q.select(root).where(spec.toPredicate(root, q, cb));
    criteria.sort().ifPresent(s ->
        q.orderBy(s.asc() ? cb.asc(root.get(s.field())) : cb.desc(root.get(s.field())))
    );

    var tq = em.createQuery(q);
    applyTenantFilter();
    return tq.getResultList();
  }

  @Override
  public Page<E> findPage(Criteria c, PageRequest page) {
    Specification<E> spec = Specs.toJpaSpecification(c);
    var cb = em.getCriteriaBuilder();

    // data
    var q = cb.createQuery(entityClass);
    var root = q.from(entityClass);
    q.select(root).where(spec.toPredicate(root, q, cb));
    c.sort().ifPresent(s ->
        q.orderBy(s.asc() ? cb.asc(root.get(s.field())) : cb.desc(root.get(s.field())))
    );
    var data = em.createQuery(q)
        .setFirstResult(page.page() * page.size())
        .setMaxResults(page.size())
        .getResultList();

    // count
    var cq = cb.createQuery(Long.class);
    var cr = cq.from(entityClass);
    cq.select(cb.count(cr)).where(spec.toPredicate(cr, cq, cb));
    long total = em.createQuery(cq).getSingleResult();

    return new Page<>(data, total, page.page(), page.size());
  }

  @Override
  public SeekPage<E> findPageBySeek(Criteria c, Sort sort, Optional<SeekKey> after, int limit) {
    var cb = em.getCriteriaBuilder();
    var q = cb.createQuery(entityClass);
    var root = q.from(entityClass);

    List<Predicate> predicates = new ArrayList<>();
    @SuppressWarnings("unchecked")
    Specification<E> spec = (Specification<E>) Specs.toJpaSpecification(c);
    predicates.add(spec.toPredicate(root, q, cb));

    after.ifPresent(sk -> {
      Object val = sk.values()[0];
      // use Expression<? extends Comparable> para evitar cast inválido
      Expression<? extends Comparable> expr = root.get(sort.field()).as(Comparable.class);
      @SuppressWarnings("unchecked")
      Predicate seekPred = sort.asc()
          ? cb.greaterThan(expr, (Comparable) val)
          : cb.lessThan(expr, (Comparable) val);
      predicates.add(seekPred);
    });

    q.select(root)
     .where(cb.and(predicates.toArray(new Predicate[0])))
     .orderBy(sort.asc() ? cb.asc(root.get(sort.field())) : cb.desc(root.get(sort.field())));

    var rows = em.createQuery(q).setMaxResults(limit).getResultList();
    Optional<SeekKey> next = rows.isEmpty()
        ? Optional.empty()
        : Optional.of(new SeekKey(new Object[]{ extractSortValue(rows.get(rows.size() - 1), sort.field()) }));

    return new SeekPage<>(rows, next);
  }

  protected Object extractSortValue(E entity, String field) {
    try {
      var m = entity.getClass().getMethod("get" + Character.toUpperCase(field.charAt(0)) + field.substring(1));
      return m.invoke(entity);
    } catch (Exception e) {
      throw new IllegalArgumentException("Não foi possível extrair sort field " + field, e);
    }
  }

  @Override
  public boolean exists(Criteria c) { return count(c) > 0; }

  @Override
  public long count(Criteria c) {
    var cb = em.getCriteriaBuilder();
    var cq = cb.createQuery(Long.class);
    var root = cq.from(entityClass);
    @SuppressWarnings("unchecked")
    Specification<E> spec = (Specification<E>) Specs.toJpaSpecification(c);
    cq.select(cb.count(root)).where(spec.toPredicate(root, cq, cb));
    return em.createQuery(cq).getSingleResult();
  }

  @Override
  public E insert(E e) {
    // garantir tenant
    if ((e.tenantId() == null || e.tenantId().isBlank())) {
      try { e.getClass().getMethod("setTenantId", String.class).invoke(e, currentTenant()); }
      catch (Exception ignored) {}
    }
    em.persist(e);
    return e;
  }

  @Override
  public E update(E e) { return em.merge(e); } // @Version cuida do optimistic lock

  @Override
  public E upsert(E e) {
    @SuppressWarnings("unchecked")
    ID id = (ID) getEntityId(e);
    if (findById(id).isPresent()) return em.merge(e);
    em.persist(e);
    return e;
  }

  @Override
  public void deleteById(ID id) {
    E e = em.find(entityClass, id);
    if (e != null) em.remove(e); // com @SQLDelete, vira soft delete
  }

  @Override
  public void delete(Criteria c) { findAll(c).forEach(em::remove); }

  @Override
  public int[] batchInsert(List<E> entities) {
    int[] res = new int[entities.size()];
    for (int i = 0; i < entities.size(); i++) {
      insert(entities.get(i));
      if (i % 50 == 0) { em.flush(); em.clear(); }
      res[i] = 1;
    }
    return res;
  }

  @Override
  public int[] batchUpdate(List<E> entities) {
    int[] res = new int[entities.size()];
    for (int i = 0; i < entities.size(); i++) {
      update(entities.get(i));
      if (i % 50 == 0) { em.flush(); em.clear(); }
      res[i] = 1;
    }
    return res;
  }

  @Override
  public void withTransaction(Runnable work) { work.run(); }

  private Object getEntityId(E e) {
    try { return e.getClass().getMethod("getId").invoke(e); }
    catch (Exception ex) { throw new IllegalStateException("Entidade precisa expor getId()", ex); }
  }

  private void applyTenantFilter() {
    Session session = em.unwrap(Session.class);
    if (session.getEnabledFilter("tenantFilter") == null) {
      session.enableFilter("tenantFilter").setParameter("tenantId", currentTenant());
    }
  }
}
