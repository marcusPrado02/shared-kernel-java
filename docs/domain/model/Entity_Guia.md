# 📘 Guia Definitivo do **Entity**

## 🔑 1. Conceito

Uma **Entity** é um **objeto de domínio com identidade persistente**.
Mesmo que seus atributos mudem, ela continua sendo a mesma.

> **Exemplo**:
>
> * Um **Cliente** muda de endereço → continua sendo o mesmo cliente.
> * Uma **Fatura** muda de status (emitida → paga) → continua sendo a mesma fatura.

### Diferença de Value Object (VO)

* **VO**: igual se valores forem iguais (`Money(10, USD)` == `Money(10, USD)`).
* **Entity**: igual se o **ID** for o mesmo, mesmo que os atributos mudem.

---

## 🏗️ 2. Estrutura no Shared-Kernel

```
sharedkernel.domain.model.base
├─ Entity.java              # base para todas as Entities
├─ AggregateRoot.java       # raiz de agregado
├─ BaseIdentifier.java      # ID forte e tipado
├─ Version.java             # controle de versão otimista
├─ TenantId.java            # suporte multi-tenant
├─ Guard.java               # invariantes declarativas
└─ DomainEvent.java         # eventos de domínio
```

---

## ⚙️ 3. Superclasse `Entity`

```java
public abstract class Entity<ID extends BaseIdentifier<?>> {

    private final ID id;
    private Version version;
    private TenantId tenant;

    protected Entity(ID id) {
        this.id = Objects.requireNonNull(id);
    }

    public ID id() { return id; }
    public Version version() { return version; }
    public TenantId tenant() { return tenant; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entity<?> other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}
```

---

## ✅ 4. Boas práticas incorporadas

* **ID imutável e tipado** (evita usar `UUID` cru em todo lugar).
* **Versionamento otimista** (`Version`) → detecção de concorrência.
* **Multi-tenant ready** (`TenantId`).
* **Invariantes explícitas** (`Guard.notNull(...)`, `Guard.that(...)`).
* **Eventos de domínio** integrados (`publishEvent(...)`).

---

## 🧩 5. Exemplos práticos

### 5.1. Entity simples: **Invoice**

```java
public final class Invoice extends Entity<Invoice.InvoiceId> {

    public static final class InvoiceId extends BaseIdentifier<String> {
        public InvoiceId(String value) { super(value); }
    }

    private final String customerId;
    private Money amount;
    private boolean paid;

    public Invoice(InvoiceId id, String customerId, Money amount) {
        super(id);
        this.customerId = Guard.notNull(customerId, "customerId");
        this.amount = Guard.notNull(amount, "amount");
        this.paid = false;
    }

    public void markAsPaid() {
        Guard.that(!paid, "already paid");
        this.paid = true;
        publishEvent(new InvoicePaidEvent(id(), amount));
    }

    public boolean isPaid() { return paid; }
    public Money amount() { return amount; }
}
```

---

### 5.2. Entity com invariantes fortes: **Subscription**

```java
public final class Subscription extends AggregateRoot<Subscription.SubscriptionId> {

    public static final class SubscriptionId extends BaseIdentifier<String> {
        public SubscriptionId(String value) { super(value); }
    }

    private String planCode;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;

    public static Subscription createNew(SubscriptionId id, String planCode, LocalDate start) {
        var s = new Subscription(id);
        s.planCode = Guard.notNull(planCode, "planCode");
        s.startDate = Guard.notNull(start, "startDate");
        s.active = true;
        s.publishEvent(new SubscriptionCreatedEvent(id));
        return s;
    }

    public void renew(LocalDate newEnd) {
        Guard.that(active, "cannot renew inactive subscription");
        Guard.that(newEnd.isAfter(startDate), "newEnd must be after startDate");
        this.endDate = newEnd;
        publishEvent(new SubscriptionRenewedEvent(id(), newEnd));
    }
}
```

---

### 5.3. Igualdade por ID

```java
var id = new Invoice.InvoiceId("inv-123");
var inv1 = new Invoice(id, "cust1", Money.of(100, "USD"));
var inv2 = new Invoice(id, "cust1", Money.of(150, "USD"));

assert inv1.equals(inv2); // true, mesmo ID
```

---

### 5.4. Entities dentro de AggregateRoot

```
Order (AggregateRoot)
 ├─ OrderItem (Entity interna)
 └─ Payment (Entity interna)
```

```java
public final class Order extends AggregateRoot<Order.OrderId> {
    private List<OrderItem> items = new ArrayList<>();

    public void addItem(OrderItem item) {
        items.add(item);
        publishEvent(new ItemAddedEvent(id(), item.id()));
    }
}
```

---

## 🌍 6. Casos reais

### E-commerce

* `Order` → raiz do agregado
* `OrderItem` → entity interna
* `Payment` → entity interna

### Billing

* `Invoice`
* `Subscription`

### IoT

* `DeviceAggregate` → raiz
* `SensorReading` → entity com timestamp + sensorId

---

## 🧪 7. Testes práticos (JUnit)

```java
@Test
void invoiceShouldBeMarkedAsPaid() {
    var inv = new Invoice(new Invoice.InvoiceId("inv-123"), "cust1", Money.of(100, "USD"));
    inv.markAsPaid();
    assertTrue(inv.isPaid());
}
```

```java
@Test
void subscriptionRenewShouldFailIfInactive() {
    var sub = Subscription.createNew(new Subscription.SubscriptionId("sub-1"), "PLAN-1", LocalDate.now());
    sub.cancel("reason");
    assertThrows(IllegalStateException.class,
        () -> sub.renew(LocalDate.now().plusDays(30)));
}
```

---

## ⚠️ 8. Erros comuns

❌ Usar `==` em vez de `.equals` para comparar Entities.
❌ Usar `UUID` cru em todo lugar (sem tipos fortes).
❌ Mutar atributos sem passar por métodos ricos de domínio.
❌ Não validar invariantes (entidades “aceitam qualquer coisa”).

---

## ❓ 9. FAQ

**🔹 Entity pode existir sem ID?**
Não, o ID é obrigatório desde a criação.

**🔹 Posso usar VO como ID da Entity?**
Sim, o recomendado é `BaseIdentifier<T>`.

**🔹 Entity dispara eventos ou só AggregateRoot?**
Boa prática: **somente o AggregateRoot** publica eventos externos. Entities internas devem sinalizar ao root.

---

## 📌 10. Conclusão

O **Entity supremo** do seu shared-kernel oferece:

* **Identidade forte e imutável**
* **Invariantes explícitas**
* **Suporte multi-tenant e versionamento**
* **Integração nativa com eventos de domínio**
* **Flexibilidade para múltiplos contextos** (billing, e-commerce, IoT, fintech)

> É a **pedra fundamental** de qualquer modelagem DDD madura.

---

👉 Quer que eu agora prepare a documentação **equivalente para o AggregateRoot supremo**, mostrando como Entities se conectam com Repositories, DomainEvents e SnapshotStrategy?
