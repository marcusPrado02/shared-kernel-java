# 📘 Guia Definitivo do **IdGenerator Supremo**

## 🔑 1. Conceito

O **IdGenerator** é a abstração responsável por **gerar identificadores únicos e estáveis** para Entities e AggregateRoots.  
Ele fornece extensibilidade para diferentes estratégias de geração, mantendo a consistência em todo o sistema.

### Por que usar?
- Evita acoplamento a UUIDs crus em todo o código.  
- Permite trocar a estratégia (UUID, ULID, Snowflake, Sequence, TimeSorted).  
- Facilita testes e simulações (mocks determinísticos).  
- Garante unicidade em sistemas distribuídos.  

---

## 🏗️ 2. Estrutura no Shared-Kernel

```
com.yourorg.sharedkernel.domain.model.base
├─ IdGenerator.java           # contrato base
├─ UuidIdGenerator.java       # implementação UUID
├─ UlidIdGenerator.java       # implementação ULID (time-sortable)
├─ SnowflakeIdGenerator.java  # implementação Twitter Snowflake
├─ SequenceIdGenerator.java   # implementação incremental (testes)
└─ exemplos/                  # uso em OrderId, SubscriptionId, etc.
```

---

## ⚙️ 3. Contrato `IdGenerator`

```java
@FunctionalInterface
public interface IdGenerator<T extends Identifier> {
    T newId();
}
```

### Design decisions
- **FunctionalInterface** → pode ser usado com lambdas.  
- **Genérico** → gera qualquer tipo de `Identifier`.  
- **Flexível** → pluggable via DI (Spring, Guice).  

---

## 🧩 4. Implementações comuns

### 4.1. UUID Generator

```java
public final class UuidIdGenerator<T extends BaseIdentifier<UUID>> implements IdGenerator<T> {
    private final Function<UUID, T> factory;
    public UuidIdGenerator(Function<UUID, T> factory) { this.factory = factory; }

    @Override public T newId() { return factory.apply(UUID.randomUUID()); }
}
```

Uso:
```java
IdGenerator<SubscriptionId> gen = new UuidIdGenerator<>(SubscriptionId::new);
SubscriptionId id = gen.newId();
```

---

### 4.2. ULID Generator (time-ordered)

```java
public final class UlidIdGenerator<T extends BaseIdentifier<String>> implements IdGenerator<T> {
    private final Function<String, T> factory;
    public UlidIdGenerator(Function<String, T> factory) { this.factory = factory; }

    @Override public T newId() { return factory.apply(de.huxhorn.sulky.ulid.ULID.random()); }
}
```

---

### 4.3. Snowflake Generator

```java
public final class SnowflakeIdGenerator<T extends BaseIdentifier<Long>> implements IdGenerator<T> {
    private final Function<Long, T> factory;
    private final Snowflake snowflake;

    public SnowflakeIdGenerator(Function<Long, T> factory, long nodeId) {
        this.factory = factory;
        this.snowflake = new Snowflake(nodeId);
    }

    @Override public T newId() { return factory.apply(snowflake.nextId()); }
}
```

---

### 4.4. Sequence Generator (testes)

```java
public final class SequenceIdGenerator<T extends BaseIdentifier<Long>> implements IdGenerator<T> {
    private final Function<Long, T> factory;
    private final AtomicLong counter = new AtomicLong(0);

    public SequenceIdGenerator(Function<Long, T> factory) {
        this.factory = factory;
    }

    @Override public T newId() { return factory.apply(counter.incrementAndGet()); }
}
```

---

## 🌍 5. Casos reais

- **E-commerce**: `OrderId` via ULID → ordenação cronológica em relatórios.  
- **Billing**: `InvoiceId` via Sequence → legibilidade e auditoria.  
- **IoT**: `SensorId` via Snowflake → alta escala, múltiplos nós.  
- **Finance**: `TransactionId` via UUID → simplicidade e robustez.  

---

## 🧪 6. Testes práticos

```java
@Test
void uuidGeneratorShouldProduceUniqueIds() {
    var gen = new UuidIdGenerator<>(SubscriptionId::new);
    var id1 = gen.newId();
    var id2 = gen.newId();
    assertNotEquals(id1, id2);
}
```

```java
@Test
void sequenceGeneratorShouldProduceIncrementalIds() {
    var gen = new SequenceIdGenerator<>(OrderId::new);
    assertEquals("1", gen.newId().asString());
    assertEquals("2", gen.newId().asString());
}
```

---

## ⚠️ 7. Erros comuns

❌ Fixar UUID cru em Entities (sem encapsular).  
❌ Usar `Random` inseguro em sistemas distribuídos.  
❌ Misturar geradores diferentes no mesmo agregado (inconsistência).  
❌ Não injetar o gerador (dificulta testes).  

---

## 📌 8. Conclusão

O **IdGenerator supremo** garante:
- Estratégias **flexíveis e pluggáveis**.  
- IDs fortes, consistentes e seguros.  
- Suporte a **cenários distribuídos, testes e ordenação temporal**.  
- Integração natural com `BaseIdentifier` e `Entity/AggregateRoot`.  

É o **motor da identidade** em sistemas distribuídos baseados em DDD.  
