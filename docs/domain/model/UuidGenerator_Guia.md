# 📘 Guia Definitivo do **UuidGenerator Supremo**

## 🔑 1. Conceito

O **UuidGenerator** é a implementação padrão de `IdGenerator` que gera identificadores únicos usando **UUID v4**.  
Ele encapsula a lógica de criação de IDs, permitindo trocar facilmente para outras estratégias (`ULID`, `UUIDv7`, `Snowflake`) sem alterar o domínio.

---

## 🏗️ 2. Estrutura no Shared-Kernel

```
com.yourorg.sharedkernel.domain.model.base
├─ IdGenerator.java       # contrato genérico
├─ UuidGenerator.java     # implementação com UUID v4
├─ UlidGenerator.java     # alternativa com ULID
├─ SnowflakeGenerator.java# alternativa distribuída
└─ exemplos/              # OrderId, SubscriptionId usando IdGenerator
```

---

## ⚙️ 3. Implementação `UuidGenerator`

```java
package com.marcusprado02.sharedkernel.domain.model.base;

import java.util.UUID;

/** Gerador default (UUID v4). Pode trocar por ULID/UUIDv7 sem tocar o domínio. */
public final class UuidGenerator<ID extends Identifier> implements IdGenerator<ID> {
    private final java.util.function.Function<String, ID> factory;

    public UuidGenerator(java.util.function.Function<String, ID> factory) {
        this.factory = factory;
    }

    @Override public ID newId() { return factory.apply(UUID.randomUUID().toString()); }
}
```

---

## ✅ 4. Boas práticas incorporadas

- **Strategy pattern** → pode ser substituído por ULID/UUIDv7 sem refatorar Entities.  
- **Factory Function** → injeta como o UUID será convertido em `BaseIdentifier`.  
- **Independência do domínio** → domínio não conhece `UUID`, só `IdGenerator`.  
- **Compatível com DI** (Spring, Guice) → pode ser registrado como `@Bean`.  

---

## 🧩 5. Exemplos práticos

### 5.1. Uso com Entity

```java
public final class OrderId extends BaseIdentifier<String> {
    public OrderId(String value) { super(value); }
}

IdGenerator<OrderId> idGen = new UuidGenerator<>(OrderId::new);
OrderId id = idGen.newId();
System.out.println(id); // "c0a8013c-9d5a-4c5a-a45f-bff0a45c41a1"
```

---

### 5.2. Criando AggregateRoot

```java
public final class Order extends AggregateRoot<OrderId> {
    private final List<OrderItem> items = new ArrayList<>();

    public static Order createNew(IdGenerator<OrderId> gen) {
        return new Order(gen.newId());
    }
}
```

---

### 5.3. Injeção no Spring

```java
@Configuration
public class IdGeneratorConfig {

    @Bean
    public IdGenerator<OrderId> orderIdGenerator() {
        return new UuidGenerator<>(OrderId::new);
    }

    @Bean
    public IdGenerator<CustomerId> customerIdGenerator() {
        return new UuidGenerator<>(CustomerId::new);
    }
}
```

---

### 5.4. Em testes (mockando)

```java
@Test
void uuidGeneratorShouldProduceDifferentIds() {
    IdGenerator<OrderId> gen = new UuidGenerator<>(OrderId::new);
    OrderId id1 = gen.newId();
    OrderId id2 = gen.newId();
    assertNotEquals(id1, id2);
}
```

---

## 🌍 6. Casos reais

- **E-commerce**: `OrderId`, `CustomerId` com UUID v4.  
- **Billing**: `SubscriptionId` com UUID v4.  
- **IoT**: `DeviceId` → fácil integração com brokers MQTT/Kafka.  
- **Finance**: `TransactionId` → unicidade global garantida.  

---

## ⚠️ 7. Erros comuns

❌ Usar `UUID.randomUUID()` direto em Entities (sem encapsular).  
❌ Fixar UUID como string em todo lugar → difícil trocar depois.  
❌ Não injetar IdGenerator em Factories → reduz testabilidade.  
❌ Confundir `UUID v4` (aleatório) com `UUID v7` (time-sortable).  

---

## 📌 8. Conclusão

O **UuidGenerator supremo** garante:
- IDs únicos, imutáveis e globais.  
- Independência de implementação (Strategy pattern).  
- Fácil integração com Entities, Aggregates e Repositórios.  
- Flexibilidade para migrar para **ULID** ou **UUIDv7** futuramente.  

É a **implementação default** para identidades em DDD, simples e robusta.  
