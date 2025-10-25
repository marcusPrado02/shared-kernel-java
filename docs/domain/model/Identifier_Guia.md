# 📘 Guia Definitivo do **Identifier Supremo**

## 🔑 1. Conceito

O **Identifier** é a **interface raiz de todos os identificadores de domínio**.  
Ele abstrai o conceito de "identidade" em DDD, garantindo que cada entidade ou agregado tenha um identificador único, forte e comparável.

### Diferença para BaseIdentifier
- **Identifier**: contrato mínimo (interface).  
- **BaseIdentifier<T>**: implementação abstrata genérica com toda a lógica comum.  

---

## 🏗️ 2. Estrutura no Shared-Kernel

```
com.yourorg.sharedkernel.domain.model.base
├─ Identifier.java        # contrato simples de identidade
├─ BaseIdentifier.java    # implementação base com equals/hashCode/toString
├─ IdGenerator.java       # geradores de IDs
├─ Entity.java            # depende de Identifier
└─ AggregateRoot.java     # idem
```

---

## ⚙️ 3. Interface `Identifier`

```java
public interface Identifier extends Serializable {
    String asString();   // representação única e estável
}
```

---

## ✅ 4. Boas práticas incorporadas

- **Simples e universal** → qualquer ID do domínio deve implementá-la.  
- **Conversão explícita para String** → útil para logs, mensagens e transporte.  
- **Serializable** → pode ser transmitido em mensageria ou persistido facilmente.  
- Usada como **chave em eventos de domínio** (`aggregateId`).  

---

## 🧩 5. Exemplos práticos

### 5.1. Implementação mínima

```java
public final class OrderId implements Identifier {
    private final String value;

    public OrderId(String value) {
        this.value = Objects.requireNonNull(value);
    }

    @Override public String asString() { return value; }

    @Override public String toString() { return value; }

    @Override public boolean equals(Object o) {
        return (o instanceof OrderId other) && value.equals(other.value);
    }

    @Override public int hashCode() { return value.hashCode(); }
}
```

---

### 5.2. Usando com BaseIdentifier

```java
public abstract class BaseIdentifier<T> implements Identifier {
    private final T value;

    protected BaseIdentifier(T value) { this.value = value; }

    public T value() { return value; }

    @Override public String asString() { return String.valueOf(value); }

    @Override public boolean equals(Object o) {
        return (o instanceof BaseIdentifier<?> other) && Objects.equals(value, other.value);
    }

    @Override public int hashCode() { return Objects.hash(value); }
}
```

Uso:

```java
public final class SubscriptionId extends BaseIdentifier<UUID> {
    public SubscriptionId(UUID value) { super(value); }
}
```

---

### 5.3. Em Domain Events

```java
public final class OrderConfirmed extends BaseDomainEvent {
    private final OrderId orderId;

    public OrderConfirmed(OrderId orderId) {
        super(null, Instant.now(), "tenant-x", orderId.asString(), EventMetadata.minimal());
        this.orderId = orderId;
    }

    @Override public Optional<Identifier> aggregateId() { return Optional.of(orderId); }
    @Override public String eventType() { return "order.confirmed.v1"; }
    @Override public int schemaVersion() { return 1; }
}
```

---

## 🌍 6. Casos reais

- **E-commerce**: `OrderId`, `CustomerId`, `ProductId`.  
- **Billing**: `InvoiceId`, `SubscriptionId`.  
- **IoT**: `DeviceId`, `SensorId`.  
- **Finance**: `PortfolioId`, `TransactionId`.  

IDs implementando **Identifier** dão **clareza semântica** e evitam trocas acidentais entre tipos diferentes.

---

## 🧪 7. Testes práticos

```java
@Test
void identifiersWithSameValueShouldBeEqual() {
    var id1 = new OrderId("ORD-123");
    var id2 = new OrderId("ORD-123");
    assertEquals(id1, id2);
    assertEquals(id1.hashCode(), id2.hashCode());
}
```

```java
@Test
void identifierShouldExposeAsString() {
    var id = new SubscriptionId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
    assertEquals("123e4567-e89b-12d3-a456-426614174000", id.asString());
}
```

---

## ⚠️ 8. Erros comuns

❌ Usar `String` ou `UUID` diretamente sem encapsular em um tipo de domínio.  
❌ Permitir `null` em IDs.  
❌ Não sobrescrever `equals`/`hashCode`.  
❌ Expor setters para modificar IDs (IDs devem ser imutáveis).  

---

## 📌 9. Conclusão

O **Identifier supremo** garante:
- Contrato universal para identidade em todo o domínio.  
- Semântica clara para cada entidade/agregado.  
- Integração natural com **DomainEvents** e **Repositories**.  
- Base segura para `BaseIdentifier<T>` e `IdGenerator`.  

É a **interface fundacional da identidade** em sistemas DDD maduros.  
