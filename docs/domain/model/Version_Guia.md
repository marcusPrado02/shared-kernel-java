# 📘 Guia Definitivo de **Version & Versioned Supremos**

## 🔑 1. Conceito

O **Version** e a interface **Versioned** são blocos centrais para implementar **versionamento otimista** em sistemas baseados em DDD.  

- **Version** → Value Object que encapsula o número da versão.  
- **Versioned** → contrato para qualquer objeto que mantenha versão.  

Isso evita **lost updates** em cenários concorrentes e permite **event sourcing** e **snapshotting** consistentes.

---

## 🏗️ 2. Estrutura no Shared-Kernel

```
com.yourorg.sharedkernel.domain.model.base
├─ Version.java            # VO com incrementos
├─ Versioned.java          # contrato para objetos versionados
├─ Entity.java             # pode implementar Versioned
├─ AggregateRoot.java      # idem
└─ Repository              # deve respeitar controle de versão
```

---

## ⚙️ 3. Implementações

### 3.1. Version

```java
package com.marcusprado02.sharedkernel.domain.model.base;

import java.util.Objects;

/** Value Object para versionamento (incrementado na persistência). */
public record Version(long value) {
    public Version {
        if (value < 0) throw new IllegalArgumentException("version must be >= 0");
    }
    public Version next() { return new Version(value + 1); }
    public static Version zero() { return new Version(0L); }
}
```

Características:
- Imutável (`record`).  
- Garante valor **>= 0**.  
- Possui método `next()` para incremento seguro.  
- `zero()` inicializa versão padrão.  

---

### 3.2. Versioned

```java
package com.marcusprado02.sharedkernel.domain.model.base;

public interface Versioned {
    long version();
}
```

Características:
- Contrato simples → qualquer entidade/VO/aggregate pode ser versionado.  
- Normalmente implementado por **AggregateRoot**.  

---

## ✅ 4. Boas práticas incorporadas

- **Sempre iniciar em `Version.zero()`** para entidades novas.  
- **Incrementar apenas na persistência** (responsabilidade do repositório/infra).  
- **Não expor setters mutáveis** para versão → deve ser controlado pelo ciclo de vida.  
- **Repositórios** devem checar versão para evitar conflitos (optimistic locking).  

---

## 🧩 5. Exemplos práticos

### 5.1. Em AggregateRoot

```java
public final class Order extends AggregateRoot<Order.OrderId> implements Versioned {

    private Version version;

    public Order(OrderId id) {
        super(id);
        this.version = Version.zero();
    }

    @Override
    public long version() { return version.value(); }

    public void applyPersistedVersion(Version v) { this.version = v; }
}
```

---

### 5.2. Em Repository (optimistic locking)

```java
public void save(Order order) {
    long currentVersion = jdbc.queryForObject("SELECT version FROM orders WHERE id=?", Long.class, order.id().asString());
    if (currentVersion != order.version()) {
        throw new OptimisticLockingFailureException("Version mismatch");
    }
    jdbc.update("UPDATE orders SET version=?, data=? WHERE id=?",
        order.version() + 1, serialize(order), order.id().asString());
}
```

---

### 5.3. Em Event Sourcing

```java
public final class EventStore {
    public void append(AggregateRoot<?> agg, List<DomainEvent> events) {
        long expected = agg.version();
        var stored = loadEvents(agg.id());
        if (stored.size() != expected) throw new ConcurrencyException();
        persist(events, expected + 1);
    }
}
```

---

## 🌍 6. Casos reais

- **E-commerce**: evitar confirmação duplicada de `Order`.  
- **Billing**: impedir cobrança duas vezes da mesma `Invoice`.  
- **IoT**: garantir que atualização de `DeviceConfig` não sobrescreva alterações concorrentes.  
- **Finance**: manter consistência em `Portfolio` ao processar ordens simultâneas.  

---

## 🧪 7. Testes práticos

```java
@Test
void versionShouldStartAtZero() {
    var v = Version.zero();
    assertEquals(0, v.value());
}

@Test
void nextShouldIncrementVersion() {
    var v1 = Version.zero();
    var v2 = v1.next();
    assertEquals(1, v2.value());
}
```

```java
@Test
void repositoryShouldThrowOnConcurrentUpdate() {
    var order = Order.createNew(new OrderId("O1"));
    order.applyPersistedVersion(new Version(1));
    assertThrows(OptimisticLockingFailureException.class, () -> repo.save(order));
}
```

---

## ⚠️ 8. Erros comuns

❌ Incrementar versão dentro do domínio (ao invés do repositório).  
❌ Permitir valor negativo em versão.  
❌ Ignorar versão em persistência → risco de overwrites silenciosos.  
❌ Confundir versionamento de dados (optimistic lock) com versionamento de schema (eventType v1/v2).  

---

## 📌 9. Conclusão

Os **Version & Versioned supremos** garantem:
- **Controle de concorrência seguro** via optimistic locking.  
- **Imutabilidade e clareza** com `record Version`.  
- **Integração com Event Sourcing e Repositórios**.  
- **Padronização em todo o domínio**.  

São a **fundação para consistência** em sistemas distribuídos e concorrentes.  
