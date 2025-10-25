# 📘 Guia Definitivo de **ValueObject & AbstractValueObject Supremos**

## 🔑 1. Conceito

O **Value Object (VO)** é um padrão central em DDD que representa **conceitos definidos somente por seus atributos**.  
Diferente de uma **Entity**, o VO **não possui identidade própria**: se todos os atributos forem iguais, os objetos são considerados iguais.

No shared-kernel temos dois blocos fundamentais:
- **ValueObject** → interface de marcação semântica.  
- **AbstractValueObject** → classe base que implementa `equals/hashCode/toString` de forma padronizada.  

---

## 🏗️ 2. Estrutura no Shared-Kernel

```
com.yourorg.sharedkernel.domain.model.value
├─ ValueObject.java            # contrato semântico
├─ AbstractValueObject.java    # base utilitária com igualdade por valor
├─ exemplos/                   # Money, Percentage, CurrencyPair, GeoHash...
```

---

## ⚙️ 3. Implementações

### 3.1. ValueObject

```java
package com.marcusprado02.sharedkernel.domain.model.value;

import java.io.Serializable;

/** Marcador semântico para VOs (imutáveis e com igualdade por valor). */
public interface ValueObject extends Serializable {}
```

---

### 3.2. AbstractValueObject

```java
package com.marcusprado02.sharedkernel.domain.model.value;

import java.util.Arrays;
import java.util.Objects;

/**
 * Base utilitária: subclasses implementam equalityComponents() e
 * ganham equals/hashCode/toString consistentes.
 */
public abstract class AbstractValueObject implements ValueObject {

    protected abstract Object[] equalityComponents();

    @Override public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || o.getClass() != getClass()) return false;
        return Arrays.equals(equalityComponents(), ((AbstractValueObject) o).equalityComponents());
    }

    @Override public final int hashCode() {
        return Arrays.hashCode(equalityComponents());
    }

    @Override public String toString() {
        return getClass().getSimpleName() + Arrays.toString(equalityComponents());
    }

    protected static <T> T req(T v, String name) {
        return Objects.requireNonNull(v, name + " must not be null");
    }
}
```

Características:
- **`equalityComponents()`** define quais campos entram na igualdade.  
- `equals/hashCode` são **gerados automaticamente**.  
- `toString()` exibe todos os componentes.  
- `req()` ajuda a validar campos obrigatórios.  

---

## ✅ 4. Boas práticas incorporadas

- **Imutabilidade**: todos os atributos devem ser `final`.  
- **Sem identidade própria**: igualdade só pelos valores.  
- **Sempre validar invariantes no construtor**.  
- **Extender AbstractValueObject** para reduzir boilerplate.  

---

## 🧩 5. Exemplos práticos

### 5.1. Money

```java
public final class Money extends AbstractValueObject {

    private final BigDecimal amount;
    private final Currency currency;

    public Money(BigDecimal amount, Currency currency) {
        this.amount = req(amount, "amount");
        this.currency = req(currency, "currency");
        if (amount.scale() > currency.getDefaultFractionDigits()) {
            throw new IllegalArgumentException("too many decimal places");
        }
    }

    @Override
    protected Object[] equalityComponents() {
        return new Object[]{ amount.stripTrailingZeros(), currency };
    }

    public BigDecimal amount() { return amount; }
    public Currency currency() { return currency; }

    public Money add(Money other) {
        if (!currency.equals(other.currency)) throw new IllegalArgumentException("currency mismatch");
        return new Money(amount.add(other.amount), currency);
    }
}
```

---

### 5.2. Percentage

```java
public final class Percentage extends AbstractValueObject {
    private final BigDecimal value;

    public Percentage(BigDecimal value) {
        this.value = req(value, "percentage");
        if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("must be between 0 and 1");
        }
    }

    @Override protected Object[] equalityComponents() {
        return new Object[]{ value.stripTrailingZeros() };
    }

    public BigDecimal value() { return value; }
}
```

---

### 5.3. GeoHash

```java
public final class GeoHash extends AbstractValueObject {
    private final String hash;

    public GeoHash(String hash) {
        this.hash = req(hash, "hash");
    }

    @Override protected Object[] equalityComponents() {
        return new Object[]{ hash };
    }

    public String hash() { return hash; }
}
```

---

## 🌍 6. Casos reais

- **E-commerce**: `Money`, `Discount`, `CurrencyPair`.  
- **Billing**: `Percentage`, `TaxRate`.  
- **IoT**: `GeoHash`, `SensorReadingValue`.  
- **Finance**: `TickerSymbol`, `ExchangeRate`.  

---

## 🧪 7. Testes práticos

```java
@Test
void moneyShouldBeEqualByValue() {
    var m1 = new Money(new BigDecimal("10.00"), Currency.getInstance("USD"));
    var m2 = new Money(new BigDecimal("10.0"), Currency.getInstance("USD"));
    assertEquals(m1, m2);
}
```

```java
@Test
void percentageMustBeBetweenZeroAndOne() {
    assertThrows(IllegalArgumentException.class, () -> new Percentage(new BigDecimal("2.0")));
}
```

---

## ⚠️ 8. Erros comuns

❌ Usar VO mutável → quebra invariantes.  
❌ Esquecer de sobrescrever `equalityComponents()`.  
❌ Usar VOs como Entity sem ID → semântica errada.  
❌ Não validar atributos obrigatórios.  

---

## 📌 9. Conclusão

Os **ValueObject & AbstractValueObject supremos** garantem:
- **Igualdade por valor** sem boilerplate.  
- **Imutabilidade e consistência** em todo o domínio.  
- **Uso expressivo** em Entities, Aggregates e Policies.  
- **Segurança semântica** contra erros sutis.  

São a **espinha dorsal da modelagem rica de domínio**.  
