# 📘 Guia Definitivo do **SoftDeletable Supremo**

## 🔑 1. Conceito

O **SoftDeletable** é um contrato que define a capacidade de uma entidade ser **marcada como excluída sem remoção física** do banco de dados ou storage.  
Em vez de apagar registros, marcamos com um **flag lógico** (`deleted = true`), preservando histórico e auditabilidade.

Benefícios:
- **Auditabilidade** → mantém registros antigos para rastreabilidade.  
- **Consistência** → evita problemas de FK e integridade referencial.  
- **Recuperação** → itens podem ser restaurados facilmente.  

---

## 🏗️ 2. Estrutura no Shared-Kernel

```
com.yourorg.sharedkernel.domain.model.base
├─ SoftDeletable.java        # contrato de deleção lógica
├─ Entity.java               # pode implementar SoftDeletable
├─ AggregateRoot.java        # idem
├─ Repository                # deve respeitar SoftDeletable
└─ Policy/Filter             # pode aplicar exclusão condicional
```

---

## ⚙️ 3. Interface `SoftDeletable`

```java
package com.marcusprado02.sharedkernel.domain.model.base;

public interface SoftDeletable {
    boolean deleted();
}
```

---

## ✅ 4. Boas práticas incorporadas

- **Interface minimalista** → apenas contrato (`deleted()`).  
- **Flexível** → implementado por `Entity` ou `AggregateRoot`.  
- **Combinável** → pode ser usado com `Version`, `TenantId`, `Audit`.  
- **Infra-aware** → repositórios e queries devem respeitar o flag.  

---

## 🧩 5. Exemplos práticos

### 5.1. Implementando em Entity

```java
public final class Customer extends AggregateRoot<Customer.CustomerId> implements SoftDeletable {

    private boolean deleted;

    public void markAsDeleted(String actor) {
        this.deleted = true;
        this.setAudit(actor);
        publishEvent(new CustomerDeleted(id()));
    }

    @Override
    public boolean deleted() { return deleted; }
}
```

---

### 5.2. Filtro em Repository

```java
public interface CustomerRepository {
    Optional<Customer> findById(Customer.CustomerId id);
    List<Customer> findAllActive(); // apenas não deletados
}
```

```java
public class JpaCustomerRepository implements CustomerRepository {
    @PersistenceContext private EntityManager em;

    @Override
    public Optional<Customer> findById(Customer.CustomerId id) {
        return Optional.ofNullable(em.find(Customer.class, id));
    }

    @Override
    public List<Customer> findAllActive() {
        return em.createQuery("SELECT c FROM Customer c WHERE c.deleted = false", Customer.class)
                 .getResultList();
    }
}
```

---

### 5.3. Estratégia com `@Where` do Hibernate

```java
@Entity
@Where(clause = "deleted = false")
public class Customer extends AggregateRoot<Customer.CustomerId> implements SoftDeletable {
    private boolean deleted;
    @Override public boolean deleted() { return deleted; }
}
```

---

### 5.4. Restore de Entity

```java
public void restore(String actor) {
    Guard.that(deleted, "cannot restore an active entity");
    this.deleted = false;
    this.setAudit(actor);
    publishEvent(new CustomerRestored(id()));
}
```

---

## 🌍 6. Casos reais

- **E-commerce**: `Product`, `Customer`, `Cart` → exclusão lógica.  
- **Billing**: `Invoice` e `Subscription` → preservação de histórico.  
- **IoT**: `Device` → desativação lógica sem perda de dados.  
- **Finance**: `Portfolio` → manter registro mesmo inativo.  

---

## 🧪 7. Testes práticos

```java
@Test
void entityShouldBeMarkedAsDeleted() {
    var c = new Customer(new Customer.CustomerId("C1"));
    c.markAsDeleted("system");
    assertTrue(c.deleted());
}
```

```java
@Test
void deletedEntitiesShouldBeFilteredOut() {
    var active = repo.findAllActive();
    assertTrue(active.stream().noneMatch(Customer::deleted));
}
```

---

## ⚠️ 8. Erros comuns

❌ Usar `delete()` físico e perder histórico.  
❌ Não filtrar `deleted = true` em queries → resultados poluídos.  
❌ Não gerar eventos ao marcar como deletado/restaurado.  
❌ Confundir `deleted` com `active` (semântica diferente).  

---

## 📌 9. Conclusão

O **SoftDeletable supremo** garante:
- Exclusão lógica **padronizada e auditável**.  
- Melhor **consistência de dados** em sistemas distribuídos.  
- Integração nativa com `Repositories`, `Events`, `Policies`.  
- Flexibilidade para **delete** e **restore** seguro.  

É o **padrão recomendado para domínios críticos** que não podem perder histórico.  
