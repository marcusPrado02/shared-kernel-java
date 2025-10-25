package com.marcusprado02.sharedkernel.saga.store;


import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcusprado02.sharedkernel.saga.JsonSagaData;
import com.marcusprado02.sharedkernel.saga.SagaData;
import com.marcusprado02.sharedkernel.saga.SagaInstance;
import com.marcusprado02.sharedkernel.saga.SagaStore;
import com.marcusprado02.sharedkernel.saga.model.SagaInstanceEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

public class JpaSagaStore implements SagaStore {
  private final EntityManager em;
  private final ObjectMapper om;

  public JpaSagaStore(EntityManager em, ObjectMapper om) {
    this.em = em;
    this.om = om;
  }

  @Override
  public <D extends SagaData> void save(SagaInstance<D> s){
    var e = toEntity(s);
    em.persist(e);
  }

  @Override
  public <D extends SagaData> Optional<SagaInstance<D>> find(String sagaId, Class<D> type){
    var e = em.find(SagaInstanceEntity.class, sagaId);
    if (e == null) return Optional.empty();
    var s = fromEntity(e, type);
    return Optional.of(s);
  }

  @Override
  public boolean tryUpdateVersion(String sagaId, int expected, Consumer<SagaInstance<?>> mut){
    var e = em.find(SagaInstanceEntity.class, sagaId, LockModeType.OPTIMISTIC);
    if (e == null) return false;

    // com campos públicos:
    if (!Objects.equals(e.version, expected)) return false;

    // carrega uma instância "raw" para o mutator aplicar alterações genéricas
    var s = fromEntityRaw(e);
    mut.accept(s);

    // copia de volta os campos alterados
    e.status      = s.status();                         // <-- ajuste se seu getter tiver outro nome
    e.currentStep = s.currentStep();                    // <-- idem
    try {
      e.dataJson = om.writeValueAsString(s.data());     // serialize (antes estava /*serialize*/)
    } catch (Exception ex) {
      throw new IllegalStateException("serialize saga data", ex);
    }
    e.version   = expected + 1;
    e.updatedAt = OffsetDateTime.now();

    em.merge(e);
    return true;
  }

  // ----------------- Helpers de mapeamento -----------------

  /** Mapeia do modelo de domínio para a entidade JPA. */
  private <D extends SagaData> SagaInstanceEntity toEntity(SagaInstance<D> s) {
    var e = new SagaInstanceEntity();

    // Se sua entity usa getters/setters, troque por e.setSagaId(...), etc.
    e.sagaId      = s.sagaId();             // <-- ajuste se for getId()
    e.status      = s.status();         // <-- ajuste se for getStatus()
    e.currentStep = s.currentStep();    // <-- ajuste se for getCurrentStep()
    try {
      e.dataJson = om.writeValueAsString(s.data());
    } catch (Exception ex) {
      throw new IllegalStateException("serialize saga data", ex);
    }
    e.version   = s.version();          // <-- ajuste se for getVersion()
    e.updatedAt = s.updatedAt() != null ? OffsetDateTime.ofInstant(s.updatedAt(), OffsetDateTime.now().getOffset()) : OffsetDateTime.now();
    return e;
  }

  /** Mapeia da entidade JPA para o modelo de domínio com o tipo forte D. */
  private <D extends SagaData> SagaInstance<D> fromEntity(SagaInstanceEntity e, Class<D> type) {
    try {
      var data = om.readValue(e.dataJson, type);
      // ⚠️ Troque SagaInstance.of(...) pela sua fábrica real (builder/ctor)
      return SagaInstance.of(
          e.sagaId,          // id
          e.sagaName,        // sagaName
          e.status,          // status
          e.currentStep,     // currentStep
          data,              // data (D)
          e.version,         // version
          e.updatedAt        // updatedAt
      );
    } catch (Exception ex) {
      throw new IllegalStateException("deserialize saga data", ex);
    }
  }

  /**
   * Versão "raw" (data como Map) para permitir mutações genéricas sem conhecer D.
   * O mutator pode trocar status, step e conteúdo do data (Map).
   */
  @SuppressWarnings("unchecked")
  private SagaInstance<?> fromEntityRaw(SagaInstanceEntity e) {
    try {
      var data = (Map<String,Object>) om.readValue(e.dataJson, Map.class);
      return SagaInstance.of(
          e.sagaId,
          e.sagaName,
          e.status,
          e.currentStep,
          new JsonSagaData(data),
          e.version,
          e.updatedAt
      );
    } catch (Exception ex) {
      throw new IllegalStateException("deserialize saga data (raw)", ex);
    }
  }
}