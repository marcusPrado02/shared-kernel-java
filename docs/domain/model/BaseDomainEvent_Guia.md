# 📘 Guia Definitivo do **BaseDomainEvent Supremo**

## 🔑 1. Conceito

Um **DomainEvent** representa **algo que já aconteceu no domínio** e que é relevante para o negócio.  
O **BaseDomainEvent** no shared-kernel padroniza:  
- **Identidade única do evento** (idempotência).  
- **Metadados de rastreio** (correlationId, causationId, actor).  
- **Versionamento de schema** (compatibilidade forward/backward).  
- **Tenant-awareness** e **partitionKey** (útil para multitenancy e roteamento em filas).  

---

## 🏗️ 2. Estrutura no Shared-Kernel

```
com.yourorg.sharedkernel.domain.event
├─ DomainEvent.java          # contrato base
├─ BaseDomainEvent.java      # implementação abstrata com defaults
├─ EventMetadata.java        # metadados (actor, correlation, attrs)
├─ EventEnvelope.java        # wrapper para transporte
└─ examples/                 # eventos de exemplo (OrderConfirmed, etc.)
```

---

## ⚙️ 3. Contrato DomainEvent

```java
public interface DomainEvent extends Serializable {
    UUID eventId();                  // id único
    Instant occurredAt();            // quando aconteceu
    String eventType();              // nome versionado: "order.confirmed.v1"
    int schemaVersion();             // versão do payload
    Optional<Identifier> aggregateId();
    Optional<String> tenantId();
    Optional<String> partitionKey();
    EventMetadata metadata();
}
```

---

## ⚙️ 4. Classe `BaseDomainEvent`

```java
public abstract class BaseDomainEvent implements DomainEvent {

    private final UUID eventId;
    private final Instant occurredAt;
    private final String tenantId;
    private final String partitionKey;
    private final EventMetadata metadata;

    protected BaseDomainEvent(UUID eventId, Instant occurredAt, String tenantId, 
                              String partitionKey, EventMetadata metadata) {
        this.eventId = eventId == null ? UUID.randomUUID() : eventId;
        this.occurredAt = occurredAt == null ? Instant.now() : occurredAt;
        this.tenantId = tenantId;
        this.partitionKey = partitionKey;
        this.metadata = metadata == null ? EventMetadata.minimal() : metadata;
    }

    @Override public final UUID eventId() { return eventId; }
    @Override public final Instant occurredAt() { return occurredAt; }
    @Override public final Optional<String> tenantId() { return Optional.ofNullable(tenantId); }
    @Override public final Optional<String> partitionKey() { return Optional.ofNullable(partitionKey); }
    @Override public final EventMetadata metadata() { return metadata; }

    @Override public abstract String eventType();
    @Override public abstract int schemaVersion();
    @Override public abstract Optional<Identifier> aggregateId();
}
```

---

## 🧩 5. Exemplo: OrderConfirmed

```java
public final class OrderConfirmed extends BaseDomainEvent {

    private final OrderId orderId;
    private final Money total;
    private static final int SCHEMA = 1;

    public OrderConfirmed(OrderId orderId, Money total, String tenantId, String partitionKey,
                          EventMetadata meta, UUID eventId, Instant occurredAt) {
        super(eventId, occurredAt, tenantId, partitionKey, meta);
        this.orderId = orderId;
        this.total = total;
    }

    public static OrderConfirmed of(OrderId orderId, Money total, String tenantId, String partitionKey, EventMetadata meta) {
        return new OrderConfirmed(orderId, total, tenantId, partitionKey, meta, null, null);
    }

    @Override public String eventType() { return "order.confirmed.v" + SCHEMA; }
    @Override public int schemaVersion() { return SCHEMA; }
    @Override public Optional<Identifier> aggregateId() { return Optional.of(orderId); }

    public OrderId orderId() { return orderId; }
    public Money total() { return total; }
}
```

---

## 🌍 6. Casos reais

- **E-commerce**: `OrderConfirmed`, `OrderCancelled`, `PaymentAuthorized`.  
- **Finance**: `PortfolioRebalanced`, `DividendPaid`.  
- **IoT**: `SensorReadingRecorded`, `DeviceActivated`.  

Cada evento comunica mudanças que interessam a **outros bounded contexts**.

---

## 🔗 7. Integração

### 7.1. Envelope de Transporte

```java
EventEnvelope<OrderConfirmed> env = EventEnvelope.of(
    "orders", orderId.asString(),
    OrderConfirmed.of(orderId, total, tenant.asString(), orderId.asString(), EventMetadata.minimal()),
    Map.of("traceparent", "00-...")
);
```

### 7.2. Persistência via Outbox

```java
var events = order.pullEvents();
outbox.saveAll(events); // serializa BaseDomainEvent em JSON/Avro/Protobuf
```

### 7.3. Upcasters

Quando evoluir schema (`.v2`), use **EventMigrator/Upcaster** para converter eventos antigos → novos.

---

## 🧪 8. Testes

```java
@Test
void shouldCreateEventWithDefaults() {
    var evt = OrderConfirmed.of(new OrderId("ORD-123"), Money.of(50,"USD"), "acme", "ORD-123", EventMetadata.minimal());
    assertEquals("order.confirmed.v1", evt.eventType());
    assertNotNull(evt.eventId());
    assertTrue(evt.occurredAt().isBefore(Instant.now()));
}
```

---

## ⚠️ 9. Erros comuns

❌ Não versionar `eventType`.  
❌ Usar campos mutáveis em evento (eventos devem ser imutáveis).  
❌ Publicar evento direto de Entities internas (só AggregateRoot deve emitir).  
❌ Não incluir `correlationId`/`causationId`, dificultando tracing.  

---

## 📌 10. Conclusão

O **BaseDomainEvent supremo** garante:  
- Identidade única e idempotência.  
- Versionamento explícito de schema.  
- Multi-tenant e roteamento via partitionKey.  
- Integração limpa com Outbox, CDC, EventStore.  
- Testabilidade e imutabilidade.

É a **coluna vertebral da comunicação assíncrona** em uma arquitetura DDD + Event-Driven.

