# 📘 Guia Definitivo do **Guard Supremo**

## 🔑 1. Conceito

O **Guard** é um utilitário central no shared-kernel para **proteger invariantes de domínio**.  
Ele substitui `if (...) throw` espalhados pelo código por chamadas **semânticas, padronizadas e autoexplicativas**.

Benefícios:
- Código de domínio mais limpo e legível.
- Redução de duplicação de validações.
- Invariantes explícitas e consistentes.

---

## 🏗️ 2. Estrutura no Shared-Kernel

```
com.yourorg.sharedkernel.domain.model.base
├─ Guard.java              # utilitário estático
├─ Entity.java             # usa Guard.notNull / Guard.that
├─ AggregateRoot.java      # idem
├─ BaseIdentifier.java
└─ exemplos/               # Subscription, Order, Invoice
```

---

## ⚙️ 3. API `Guard`

```java
public final class Guard {

    private Guard() {}

    public static <T> T notNull(T value, String fieldName) {
        if (value == null) throw new IllegalArgumentException(fieldName + " must not be null");
        return value;
    }

    public static String notBlank(String value, String fieldName) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException(fieldName + " must not be blank");
        return value;
    }

    public static void that(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    public static void that(boolean condition, Supplier<String> messageSupplier) {
        if (!condition) throw new IllegalStateException(messageSupplier.get());
    }

    public static void positive(BigDecimal number, String fieldName) {
        if (number == null || number.signum() <= 0)
            throw new IllegalArgumentException(fieldName + " must be positive");
    }

    public static void nonNegative(BigDecimal number, String fieldName) {
        if (number == null || number.signum() < 0)
            throw new IllegalArgumentException(fieldName + " must be non-negative");
    }
}
```

---

## ✅ 4. Boas práticas incorporadas

- **Overloads para Supplier<String>** → mensagens só construídas quando necessário.  
- **Métodos semânticos**: `notNull`, `notBlank`, `positive`, `that`.  
- **Lançamento de exceções específicas** (`IllegalArgumentException`, `IllegalStateException`).  
- **Genérico** → pode ser usado em qualquer camada (domínio, aplicação, infra).  

---

## 🧩 5. Exemplos práticos

### 5.1. Validando Entity

```java
public final class Subscription extends AggregateRoot<Subscription.SubscriptionId> {

    private String planCode;
    private LocalDate startDate;

    public static Subscription createNew(SubscriptionId id, String plan, LocalDate start) {
        var s = new Subscription(id);
        s.planCode = Guard.notBlank(plan, "planCode");
        s.startDate = Guard.notNull(start, "startDate");
        return s;
    }
}
```

---

### 5.2. Regras de negócio

```java
public void renew(LocalDate newEnd) {
    Guard.that(active, "cannot renew inactive subscription");
    Guard.notNull(newEnd, "newEndDate");
    Guard.that(newEnd.isAfter(startDate), () -> "newEnd must be after " + startDate);
    this.endDate = newEnd;
}
```

---

### 5.3. Validando VO

```java
public final class Percentage extends AbstractValueObject {
    private final BigDecimal value;

    public Percentage(BigDecimal value) {
        Guard.notNull(value, "percentage");
        Guard.that(value.compareTo(BigDecimal.ZERO) >= 0 && value.compareTo(BigDecimal.ONE) <= 0,
                   "percentage must be between 0 and 1");
        this.value = value;
    }
}
```

---

### 5.4. Validando Input de Serviço

```java
public Money convert(Money amount, CurrencyPair pair) {
    Guard.notNull(amount, "amount");
    Guard.positive(amount.amount(), "amount");
    Guard.notNull(pair, "currencyPair");
    return amount.convert(pair);
}
```

---

## 🌍 6. Casos reais

- **E-commerce**: impedir criar `Order` sem `OrderItem`.  
- **Billing**: validar que `Invoice` tem `dueDate` futura.  
- **IoT**: garantir que `SensorReading` tenha valor positivo.  
- **Finance**: assegurar que `Transaction` tem montante não negativo.  

---

## 🧪 7. Testes práticos

```java
@Test
void notNullShouldThrowWhenNull() {
    assertThrows(IllegalArgumentException.class, () -> Guard.notNull(null, "field"));
}

@Test
void thatShouldThrowWhenFalse() {
    assertThrows(IllegalStateException.class, () -> Guard.that(1 > 2, "impossible"));
}

@Test
void positiveShouldAcceptPositive() {
    Guard.positive(BigDecimal.ONE, "amount"); // não lança exceção
}
```

---

## ⚠️ 8. Erros comuns

❌ Usar `if (x == null) throw ...` espalhado no código.  
❌ Mensagens de erro inconsistentes.  
❌ Não validar entradas em Factories → invariantes quebradas logo na criação.  
❌ Confundir exceções: usar `IllegalStateException` quando devia ser `IllegalArgumentException`.  

---

## 📌 9. Conclusão

O **Guard supremo** garante:
- **Invariantes explícitas** no domínio.  
- **Código mais limpo e padronizado**.  
- **Maior robustez contra erros de uso**.  
- **Compatível com Entities, VOs, Services e Policies**.  

É a **primeira linha de defesa** para manter a integridade do modelo de domínio.  
