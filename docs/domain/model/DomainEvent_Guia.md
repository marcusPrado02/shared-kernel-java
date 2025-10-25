# 📘 Guia Definitivo do **DomainEvent Supremo**

## 🔑 1. Conceito

Um **DomainEvent** é a representação explícita de um **fato já ocorrido no domínio**.  
Ele é fundamental em arquiteturas **DDD + Event-Driven** pois permite:  
- Notificar outros bounded contexts.  
- Propagar mudanças de estado.  
- Servir como fonte única da verdade em Event Sourcing.

### Diferença de Command x Event
- **Command**: pedido de execução (imperativo) → *"Ship Order"*.  
- **DomainEvent**: algo que já aconteceu (fato imutável) → *"Order Shipped"*.  

---

## 🏗️ 2. Estrutura no Shared-Kernel

```
com.yourorg.sharedkernel.domain.event
├─ DomainEvent.java        # contrato raiz (interface)
├─ BaseDomainEvent.java    # implementação abstrata com metadados e defaults
├─ EventMetadata.java      # correlation, causation, actor
├─ EventEnvelope.java      # wrapper para transporte em filas/tópicos
└─ examples/               # eventos concretos
```

---

## ⚙️ 3. Contrato `DomainEvent`

```java
public interface DomainEvent extends Serializable {
    UUID eventId();                   // id único do evento
    Instant occurredAt();             // quando aconteceu
    String eventType();               // nome único versionado ("order.shipped.v1")
    int schemaVersion();              // versão do payload
    Optional<Identifier> aggregateId();
    Optional<String> tenantId();
    Optional<String> partitionKey();
    EventMetadata metadata();
}
```

### Design decisions
- **Imutável**: deve ser criado pronto, sem setters.  
- **Versionado**: `eventType` + `schemaVersion` → evolução segura.  
- **Idempotência**: `eventId` garante reprocessamento seguro.  
- **Traceability**: `correlationId` e `causationId` ligam eventos em sagas.  

---

## 🧩 4. Exemplo simples: `OrderShipped`

```java
public final class OrderShipped extends BaseDomainEvent {

    private final OrderId orderId;
    private final Instant shippedAt;
    private static final int SCHEMA = 1;

    public OrderShipped(OrderId orderId, Instant shippedAt, String tenant, String partition, EventMetadata meta) {
        super(null, shippedAt, tenant, partition, meta);
        this.orderId = orderId;
        this.shippedAt = shippedAt;
    }

    public static OrderShipped of(OrderId orderId, Instant shippedAt, String tenant) {
        return new OrderShipped(orderId, shippedAt, tenant, orderId.asString(), EventMetadata.minimal());
    }

    @Override public String eventType() { return "order.shipped.v" + SCHEMA; }
    @Override public int schemaVersion() { return SCHEMA; }
    @Override public Optional<Identifier> aggregateId() { return Optional.of(orderId); }

    public OrderId orderId() { return orderId; }
    public Instant shippedAt() { return shippedAt; }
}
```

---

## 🌍 5. Casos reais

- **E-commerce**: `OrderPlaced`, `OrderPaid`, `OrderShipped`, `OrderCancelled`.  
- **Billing**: `InvoiceIssued`, `PaymentProcessed`, `SubscriptionRenewed`.  
- **IoT**: `SensorReadingCaptured`, `DeviceCalibrated`.  
- **Finance**: `PortfolioRebalanced`, `DividendPaid`.  

---

## 🔗 6. Integrações

### 6.1. Outbox Pattern
```java
var order = orderRepo.findById(id).orElseThrow();
order.confirm(actor);
orderRepo.save(order);

var events = order.pullEvents();
outbox.saveAll(events); // salva eventos para processamento assíncrono
```

### 6.2. EventEnvelope para transporte
```java
EventEnvelope<OrderShipped> env = EventEnvelope.of(
    "orders", orderId.asString(),
    OrderShipped.of(orderId, Instant.now(), "acme"),
    Map.of("traceparent", "00-...")
);
```
Transportado por Kafka, RabbitMQ, SNS/SQS, etc.

### 6.3. Upcasters (migração de schema)
Quando mudar de `v1` para `v2`, um **Upcaster** traduz eventos antigos para o novo formato.  

```java
public final class OrderShippedV1ToV2Upcaster implements Upcaster {
    @Override public boolean supports(String eventType) {
        return eventType.equals("order.shipped.v1");
    }
    @Override public DomainEvent upcast(DomainEvent oldEvent) {
        var v1 = (OrderShippedV1) oldEvent;
        return new OrderShippedV2(v1.orderId(), v1.shippedAt(), "standard"); // adiciona campo deliveryType
    }
}
```

---

## 🧪 7. Testes práticos

```java
@Test
void shouldCreateEventWithDefaults() {
    var evt = OrderShipped.of(new OrderId("ORD-1"), Instant.now(), "acme");
    assertEquals("order.shipped.v1", evt.eventType());
    assertNotNull(evt.eventId());
    assertTrue(evt.occurredAt().isBefore(Instant.now()));
}
```

---

## ⚠️ 8. Erros comuns

❌ Publicar eventos a partir de Entities internas (apenas AggregateRoot deve emitir).  
❌ Não versionar eventos (quebra consumidores).  
❌ Usar eventos mutáveis (quebra idempotência).  
❌ Não incluir metadata de tracing.  

---

## 📌 9. Conclusão

O **DomainEvent supremo** garante:  
- **Imutabilidade e idempotência**.  
- **Versionamento seguro** para evolução.  
- **Traceability** com metadata.  
- **Integração direta com Outbox e EventStore**.  
- **Padrão comum** para todos os bounded contexts.  

Ele é a **linguagem universal de fatos** no seu sistema distribuído.  
