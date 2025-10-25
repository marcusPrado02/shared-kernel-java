# 📘 Guia Definitivo do **AggregateRoot Supremo**

## 🔑 1. Conceito

Um **AggregateRoot** é uma **Entity especial** que:  
- É a **porta de entrada** para todas as operações do agregado.  
- **Orquestra invariantes** entre Entities internas.  
- **Publica eventos de domínio** (as outras entities não devem disparar direto).  
- É a **única unidade carregada e persistida** em um repositório.

> Exemplo:  
> - `Order` (AggregateRoot)  
>   - contém várias `OrderItem` (Entities internas).  
> - `Portfolio` (AggregateRoot)  
>   - contém `AssetAllocation` (Entities internas).  

---

## 🏗️ 2. Estrutura no Shared-Kernel

```
com.yourorg.sharedkernel.domain.model.base
├─ AggregateRoot.java       # superclasse base
├─ Entity.java              # base genérica de Entities
├─ BaseIdentifier.java      # ID forte
├─ Version.java             # versão para optimistic locking
├─ DomainEvent.java         # eventos de domínio
└─ Guard.java               # invariantes
```

---

## ⚙️ 3. Superclasse `AggregateRoot`

### Contrato

```java
public abstract class AggregateRoot<ID extends BaseIdentifier<?>>
        extends Entity<ID> {

    private final List<DomainEvent> pendingEvents = new ArrayList<>();

    protected AggregateRoot(ID id) { super(id); }

    /** publica evento e registra para flush posterior (Outbox/ES) */
    protected void publishEvent(DomainEvent event) {
        pendingEvents.add(Objects.requireNonNull(event));
    }

    /** coleta eventos ainda não processados */
    public List<DomainEvent> pullEvents() {
        var copy = List.copyOf(pendingEvents);
        pendingEvents.clear();
        return copy;
    }

    /** ciclo de mutação seguro (auditoria + invariantes) */
    protected void mutate(String actor, Runnable action) {
        beforeMutation(actor);
        action.run();
        validateAggregate();
        afterMutation(actor);
    }

    /** hooks de extensão */
    protected void beforeMutation(String actor) {}
    protected void afterMutation(String actor) {}

    /** cada agregado implementa suas invariantes */
    protected abstract void validateAggregate();
}
```

---

## ✅ 4. Design decisions

- **Eventos só no root**: evita *event storms* descoordenados.  
- **`mutate(actor, action)`**: garante invariantes sempre checadas **depois** da mudança.  
- **`validateAggregate()`**: obriga cada agregado a declarar suas invariantes.  
- **`pullEvents()`**: integra com Outbox ou EventStore sem expor lista mutável.  
- **Hooks** (`beforeMutation`, `afterMutation`) → permitem auditar ou aplicar policies.  

---

## 🧩 5. Exemplos práticos

### 5.1. **Order (AggregateRoot com Entities internas)**

```java
public final class Order extends AggregateRoot<Order.OrderId> {

    public static final class OrderId extends BaseIdentifier<String> {
        public OrderId(String value) { super(value); }
    }

    private final List<OrderItem> items = new ArrayList<>();
    private boolean confirmed;

    private Order(OrderId id) { super(id); }

    public static Order createNew(OrderId id, String actor) {
        var o = new Order(id);
        o.mutate(actor, () -> {
            o.confirmed = false;
            o.publishEvent(new OrderCreated(id));
        });
        return o;
    }

    public void addItem(OrderItem item, String actor) {
        mutate(actor, () -> {
            Guard.notNull(item, "item");
            items.add(item);
            publishEvent(new ItemAdded(id(), item.id()));
        });
    }

    public void confirm(String actor) {
        mutate(actor, () -> {
            Guard.that(!confirmed, "order already confirmed");
            confirmed = true;
            publishEvent(new OrderConfirmed(id()));
        });
    }

    @Override
    protected void validateAggregate() {
        Guard.that(!items.isEmpty(), "order must have at least one item");
    }
}
```

---

### 5.2. **Portfolio (AggregateRoot com políticas de consistência)**

```java
public final class Portfolio extends AggregateRoot<Portfolio.PortfolioId> {

    public static final class PortfolioId extends BaseIdentifier<String> {
        public PortfolioId(String value) { super(value); }
    }

    private final Map<String, BigDecimal> allocations = new HashMap<>();

    private Portfolio(PortfolioId id) { super(id); }

    public void rebalance(Map<String, BigDecimal> target, String actor) {
        mutate(actor, () -> {
            allocations.clear();
            allocations.putAll(target);
            publishEvent(new PortfolioRebalanced(id(), target));
        });
    }

    @Override
    protected void validateAggregate() {
        var sum = allocations.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        Guard.that(sum.compareTo(BigDecimal.ONE) == 0,
            "allocations must sum to 100%");
    }
}
```

---

## 🌍 6. Casos reais

- **E-commerce**: `Order` como raiz, `OrderItem` como entidades internas.  
- **Billing**: `Subscription` como raiz, `Invoice` como entidade ligada.  
- **IoT**: `DeviceAggregate` como raiz, `SensorReading` como entidades.  
- **Finance**: `Portfolio` como raiz, `Position` como entidades.  

---

## 🔗 7. Integrações

### 7.1. Repository

```java
public interface OrderRepository {
    Optional<Order> findById(Order.OrderId id);
    void save(Order order);
}
```

### 7.2. Outbox Pattern

```java
var order = orderRepo.findById(id).orElseThrow();
order.confirm(actor);
orderRepo.save(order);

// flush events para Outbox
var events = order.pullEvents();
outbox.saveAll(events);
```

### 7.3. SnapshotStrategy (com Event Sourcing)

```java
if (strategy.shouldSnapshot(order.version(), order.pullEvents().size(), sizeBytes)) {
    snapshotRepo.save(new Snapshot<>(order.id(), order));
}
```

### 7.4. Policy Enforcement

```java
policyHandler.enforce("order.cancellation","v1", ctx,
    () -> order.cancel("customer request", actor),
    ResponseFilter.noop());
```

---

## 🧪 8. Testes práticos

```java
@Test
void orderMustEmitEventWhenConfirmed() {
    var order = Order.createNew(new Order.OrderId("o-1"), "user:7");
    order.addItem(new OrderItem(new OrderItem.ItemId("i-1"), "SKU-123", 2), "user:7");
    order.confirm("user:7");

    var events = order.pullEvents();
    assertTrue(events.stream().anyMatch(e -> e instanceof OrderConfirmed));
}
```

---

## ⚠️ 9. Erros comuns

❌ Permitir que Entities internas publiquem eventos sozinhas.  
❌ Esquecer de chamar `validateAggregate()` → invariantes quebram silenciosamente.  
❌ Modificar atributos direto sem passar por `mutate(...)`.  
❌ Usar `==` para comparar `AggregateRoot`s → deve ser por **ID**.  

---

## ❓ 10. FAQ

**🔹 Toda Entity precisa ser AggregateRoot?**  
Não. Apenas a **raiz** do agregado é root.  

**🔹 Onde coloco lógica de consistência complexa?**  
No próprio AggregateRoot ou em um **DomainService** se cruzar vários agregados.  

**🔹 AggregateRoot pode ter ValueObjects internos?**  
Sim, é altamente recomendado.  

---

## 📌 11. Conclusão

O **AggregateRoot supremo** garante:
- Ciclo de mutação seguro (`mutate`).  
- Eventos sempre centralizados.  
- Invariantes aplicadas automaticamente.  
- Integração direta com **repositories, outbox e snapshotting**.  

É o **contrato mais importante do domínio** — organiza entidades internas, define limites de consistência e conecta o modelo a patterns modernos (event sourcing, policies, outbox).
