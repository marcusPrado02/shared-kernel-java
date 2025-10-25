# 📘 Guia Definitivo de **TenantId & TenantScoped Supremos**

## 🔑 1. Conceito

Em sistemas **multi-tenant**, precisamos isolar dados e garantir segurança entre clientes/organizações.  
No shared-kernel, isso é resolvido com dois blocos fundamentais:

- **TenantId** → Value Object tipado que representa o identificador único de um tenant.  
- **TenantScoped** → contrato para qualquer entidade/serviço que pertença a um tenant específico.  

### Benefícios
- Isolamento seguro de dados entre tenants.  
- Semântica clara no domínio.  
- Padronização para persistência, eventos e APIs.  

---

## 🏗️ 2. Estrutura no Shared-Kernel

```
com.yourorg.sharedkernel.domain.model.base
├─ TenantId.java            # ID forte para tenant
├─ TenantScoped.java        # contrato de escopo multi-tenant
├─ Entity.java              # integra com TenantId
├─ AggregateRoot.java       # idem
└─ exemplos/                # Customer, Subscription, Portfolio
```

---

## ⚙️ 3. Implementações

### 3.1. TenantId

```java
package com.marcusprado02.sharedkernel.domain.model.base;

public class TenantId extends BaseIdentifier<String> {
    public TenantId(String value) { super(value); }
}
```

Características:
- Estende `BaseIdentifier<String>`.  
- Imutável e forte → evita confusão com IDs de entidades.  

---

### 3.2. TenantScoped

```java
package com.marcusprado02.sharedkernel.domain.model.base;

public interface TenantScoped {
    String tenantId();
}
```

Características:
- Contrato mínimo para expor tenant atual.  
- Pode ser implementado por `Entity`, `AggregateRoot`, `Service`.  

---

## ✅ 4. Boas práticas incorporadas

- **TenantId sempre obrigatório** → entidades nunca devem existir sem tenant.  
- **TenantScoped** em agregados e eventos → garante rastreabilidade.  
- **Padronização** → todas as queries e policies consideram TenantId.  
- **Segurança** → evita “data leakage” entre tenants.  

---

## 🧩 5. Exemplos práticos

### 5.1. Em Entity

```java
public final class Customer extends AggregateRoot<Customer.CustomerId> implements TenantScoped {

    private final TenantId tenantId;
    private String name;

    public Customer(CustomerId id, TenantId tenantId, String name) {
        super(id);
        this.tenantId = Guard.notNull(tenantId, "tenantId");
        this.name = Guard.notBlank(name, "name");
    }

    @Override
    public String tenantId() { return tenantId.asString(); }
}
```

---

### 5.2. Em DomainEvent

```java
public final class CustomerRegistered extends BaseDomainEvent {

    private final Customer.CustomerId customerId;
    private final TenantId tenantId;

    public CustomerRegistered(Customer.CustomerId id, TenantId tenantId) {
        super(null, Instant.now(), tenantId.asString(), id.asString(), EventMetadata.minimal());
        this.customerId = id;
        this.tenantId = tenantId;
    }

    @Override public String eventType() { return "customer.registered.v1"; }
    @Override public int schemaVersion() { return 1; }
    @Override public Optional<Identifier> aggregateId() { return Optional.of(customerId); }
}
```

---

### 5.3. Em Repository (query multi-tenant)

```java
public interface CustomerRepository {
    Optional<Customer> findByIdAndTenant(Customer.CustomerId id, TenantId tenant);
    List<Customer> findAllByTenant(TenantId tenant);
}
```

---

### 5.4. Policy Enforcement

```java
policyHandler.enforce("customer.update","v1",
    ctx.withTenant(tenantId),
    () -> customer.updateName("newName", actor),
    ResponseFilter.noop());
```

---

## 🌍 6. Casos reais

- **E-commerce SaaS** → cada loja é um tenant (`TenantId`).  
- **Fintech multi-cliente** → cada banco/corretora é um tenant.  
- **IoT** → cada organização dona de dispositivos é um tenant.  
- **Educação SaaS** → cada universidade/escola é um tenant.  

---

## 🧪 7. Testes práticos

```java
@Test
void tenantScopedEntitiesMustExposeTenant() {
    var tenant = new TenantId("tenant-1");
    var c = new Customer(new Customer.CustomerId("C1"), tenant, "Alice");
    assertEquals("tenant-1", c.tenantId());
}
```

```java
@Test
void tenantIdShouldBeStronglyTyped() {
    var t1 = new TenantId("acme");
    var t2 = new TenantId("acme");
    assertEquals(t1, t2);
}
```

---

## ⚠️ 8. Erros comuns

❌ Usar `String` crua para tenant → risco de passar `CustomerId` no lugar errado.  
❌ Não validar TenantId em APIs → risco de acesso indevido.  
❌ Esquecer TenantScoped em eventos → difícil rastrear.  
❌ Usar tenant global estático (risco em sistemas distribuídos).  

---

## 📌 9. Conclusão

O **TenantId & TenantScoped supremos** garantem:
- **Isolamento lógico e seguro** entre tenants.  
- **Tipagem forte** e semântica clara.  
- **Padronização para Entities, Eventos, Repositórios**.  
- **Segurança de dados** em sistemas multi-tenant modernos.  

São a **coluna vertebral da estratégia multi-tenant** no shared-kernel.  
