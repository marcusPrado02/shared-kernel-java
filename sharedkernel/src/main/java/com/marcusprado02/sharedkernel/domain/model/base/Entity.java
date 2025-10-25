package com.marcusprado02.sharedkernel.domain.model.base;

import java.time.Instant;
import java.util.*;

/**
 * Base de Entidade DDD — focada em:
 * - ID forte tipado (Identifier)
 * - Igualdade por identidade (com ponto de extensão para proxies)
 * - Eventos de domínio agregados (uncommitted)
 * - Auditoria: createdAt/updatedAt/createdBy/updatedBy
 * - Multi-tenant opcional (tenantId)
 * - Soft-delete (deletedAt)
 * - Version (otimista) representado no domínio (incremento feito pelo repositório)
 */
public abstract class Entity<ID extends Identifier> {

    private final ID id;                        // identidade imutável
    private Version version;                    // atualizado em persistência
    private TenantId tenantId;                  // opcional
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;                   // opcional (principal id)
    private String updatedBy;                   // opcional
    private Instant deletedAt;                  // null => ativo

    // Eventos pendentes para publicação (outbox/in-memory)
    private final transient Deque<DomainEvent> domainEvents = new ArrayDeque<>();

    protected Entity(ID id) {
        this.id = Guard.notNull(id, "id");
        var now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.version = Version.zero();
    }

    /** Reconstituição a partir de persistência. */
    protected Entity(ID id, Version version, TenantId tenantId,
                     Instant createdAt, Instant updatedAt, String createdBy, String updatedBy, Instant deletedAt) {
        this.id = Guard.notNull(id, "id");
        this.version   = (version == null) ? Version.zero() : version;
        this.tenantId  = tenantId;
        this.createdAt = (createdAt == null) ? Instant.now() : createdAt;
        this.updatedAt = (updatedAt == null) ? this.createdAt : updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.deletedAt = deletedAt;
    }

    // ---------- Identidade & auditoria ----------
    public ID id() { return id; }
    public Version version() { return version; }           // setado pelo repositório
    public Optional<TenantId> tenantId() { return Optional.ofNullable(tenantId); }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public Optional<String> createdBy() { return Optional.ofNullable(createdBy); }
    public Optional<String> updatedBy() { return Optional.ofNullable(updatedBy); }
    public boolean isDeleted() { return deletedAt != null; }
    public Optional<Instant> deletedAt() { return Optional.ofNullable(deletedAt); }

    // ---------- Mutadores protegidos (domínio controla consistência) ----------
    protected void setTenant(TenantId tenantId) { this.tenantId = tenantId; touch(); }
    protected void markDeleted() { this.deletedAt = Instant.now(); touch(); }
    protected void restore() { this.deletedAt = null; touch(); }
    protected void setAudit(String actor) { this.updatedBy = actor; if (this.createdBy == null) this.createdBy = actor; touch(); }

    /** Atualiza timestamp de modificação (e.g., após mutação válida). */
    protected void touch() { this.updatedAt = Instant.now(); }

    /** Permite ao repositório sinalizar nova versão após persistir. */
    public void _onPersistedNewVersion(Version newVersion) { this.version = Guard.notNull(newVersion, "newVersion"); }

    // ---------- Eventos de domínio ----------
    protected void recordEvent(DomainEvent event) {
        domainEvents.add(Guard.notNull(event, "domain event"));
    }

    /** Retorna e limpa eventos pendentes (padrão "releaseEvents"). */
    public List<DomainEvent> pullDomainEvents() {
        var list = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return Collections.unmodifiableList(list);
    }

    // ---------- Igualdade por identidade (robusta) ----------
    @Override public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        // Mesma classe efetiva (evita igualar subclasses diferentes)
        if (!effectiveClass().equals(effectiveClassOf(o))) return false;
        var other = (Entity<?>) o;
        return id.equals(other.id);
    }

    @Override public final int hashCode() { return Objects.hash(effectiveClass(), id); }

    @Override public String toString() {
        return effectiveClass().getSimpleName() + "[id=" + id + ", ver=" + version.value() + "]";
    }

    /** Ponto de extensão para ORMs/proxies (override numa subclasse adapter). */
    protected Class<?> effectiveClass() { return getClass(); }

    private static Class<?> effectiveClassOf(Object o) { return o.getClass(); }
}