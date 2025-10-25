# 📘 Guia Definitivo do **BaseIdentifier Supremo**

## 🔑 1. Conceito

O **BaseIdentifier** é o Value Object que representa **IDs fortes e tipados** para Entities e AggregateRoots.  
Ele substitui o uso de identificadores primitivos (`String`, `UUID`, `Long`) espalhados pelo sistema, oferecendo:
- **Imutabilidade**
- **Comparação por valor**
- **Tipo específico por entidade** (evita `mixups` de IDs)
- **Integração fácil com JPA/Jackson/Avro/Protobuf**

---

## 🏗️ 2. Estrutura no Shared-Kernel

```
com.yourorg.sharedkernel.domain.model.base
├─ BaseIdentifier.java    # superclasse genérica de IDs
├─ IdGenerator.java       # gerador de IDs (UUID, ULID, Snowflake...)
├─ Entity.java            # usa BaseIdentifier
├─ AggregateRoot.java     # idem
└─ exemplos/              # implementações concretas (OrderId, SubscriptionId...)
```

---

## ⚙️ 3. Implementação `BaseIdentifier`

```java
public abstract class BaseIdentifier<T> implements Serializable, Comparable<BaseIdentifier<T>> {

    private final T value;

    protected BaseIdentifier(T value) {
        this.value = Objects.requireNonNull(value, "id must not be null");
    }

    public T value() { return value; }

    @Override
    public String toString() { return String.valueOf(value); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseIdentifier<?> other)) return false;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public int compareTo(BaseIdentifier<T> o) {
        return this.toString().compareTo(o.toString());
    }
}
```

---

## ✅ 4. Boas práticas incorporadas

- **Construtor protegido** → só subclasses concretas podem ser criadas.  
- **Null-safety** garantida (`Objects.requireNonNull`).  
- **Comparação consistente** (`equals` e `hashCode`).  
- **Ordenável** (`Comparable`) para uso em `TreeSet`, `TreeMap`.  

---

## 🧩 5. Exemplos práticos

### 5.1. Criando IDs fortes

```java
public final class OrderId extends BaseIdentifier<String> {
    public OrderId(String value) { super(value); }
}

public final class SubscriptionId extends BaseIdentifier<UUID> {
    public SubscriptionId(UUID value) { super(value); }
}
```

Uso:

```java
var orderId = new OrderId("ORD-123");
var subscriptionId = new SubscriptionId(UUID.randomUUID());
```

---

### 5.2. Igualdade por valor

```java
var id1 = new OrderId("ORD-123");
var id2 = new OrderId("ORD-123");

assert id1.equals(id2);   // true
assert id1 != id2;        // objetos diferentes, mas mesmo valor
```

---

### 5.3. Em Entities/Aggregates

```java
public final class Order extends AggregateRoot<OrderId> {
    private final List<OrderItem> items = new ArrayList<>();

    public Order(OrderId id) { super(id); }

    public void addItem(OrderItem item) {
        Guard.notNull(item, "item");
        items.add(item);
        publishEvent(new ItemAdded(id(), item.id()));
    }
}
```

---

### 5.4. Integração com JPA (Converter)

```java
@Converter(autoApply = true)
public class OrderIdConverter implements AttributeConverter<OrderId, String> {
    @Override public String convertToDatabaseColumn(OrderId attribute) {
        return attribute == null ? null : attribute.value();
    }
    @Override public OrderId convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new OrderId(dbData);
    }
}
```

---

### 5.5. Integração com Jackson

```java
public final class OrderId extends BaseIdentifier<String> {
    @JsonCreator
    public OrderId(String value) { super(value); }
}
```

Permite serializar/desserializar diretamente em JSON:

```json
{ "orderId": "ORD-123" }
```

---

### 5.6. Uso com IdGenerator

```java
public final class OrderId extends BaseIdentifier<String> {
    public OrderId(String value) { super(value); }

    public static OrderId newId(IdGenerator<OrderId> gen) { return gen.newId(); }
}
```

---

## 🌍 6. Casos reais

- **E-commerce**: `OrderId`, `ProductId`, `CustomerId`.  
- **Billing**: `InvoiceId`, `SubscriptionId`.  
- **IoT**: `DeviceId`, `SensorId`.  
- **Finance**: `PortfolioId`, `TransactionId`.  

IDs fortes evitam bugs como passar `customerId` no lugar de `orderId`.  

---

## 🧪 7. Testes práticos

```java
@Test
void idsWithSameValueShouldBeEqual() {
    var id1 = new OrderId("ORD-123");
    var id2 = new OrderId("ORD-123");
    assertEquals(id1, id2);
    assertEquals(id1.hashCode(), id2.hashCode());
}
```

```java
@Test
void idsShouldBeComparable() {
    var id1 = new OrderId("A");
    var id2 = new OrderId("B");
    assertTrue(id1.compareTo(id2) < 0);
}
```

---

## ⚠️ 8. Erros comuns

❌ Usar `String`/`UUID` cru em todo lugar.  
❌ Comparar IDs com `==` em vez de `equals`.  
❌ Esquecer de implementar `@JsonCreator`/`@Converter` → problemas de serialização.  
❌ Não versionar IDs (quando precisa migrar tipo de `Long` → `UUID`).  

---

## 📌 9. Conclusão

O **BaseIdentifier supremo** garante:
- IDs **fortes, imutáveis e tipados**.  
- Menos risco de confusão entre entidades diferentes.  
- Integração simples com JPA, Jackson, EventSourcing.  
- Suporte a ordenação e comparações consistentes.  

É a **fundação de identidades seguras** em DDD e sistemas distribuídos.  
