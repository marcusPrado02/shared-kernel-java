# 📘 Guia Definitivo de **CurrencyCode & Money Supremos**

## 🔑 1. Conceito

O pacote **money** do shared-kernel fornece **Value Objects** seguros e imutáveis para lidar com **valores monetários e moedas**.  
Ele substitui o uso cru de `BigDecimal` e `String` para representar dinheiro, garantindo **semântica forte**, **validação rigorosa** e **operações consistentes**.

- **CurrencyCode** → VO que encapsula um código ISO-4217 de moeda.  
- **Money** → VO que representa valor monetário associado a uma moeda.  

### Benefícios
- Semântica clara (`Money` ≠ `BigDecimal`).  
- Validação de moeda via ISO-4217.  
- Imutabilidade e precisão.  
- Operações seguras e semânticas (`plus`, `minus`, `times`).  

---

## 🏗️ 2. Estrutura no Shared-Kernel

```
com.yourorg.sharedkernel.domain.model.value.money
├─ CurrencyCode.java      # VO para códigos de moeda ISO-4217
├─ Money.java             # VO para valores monetários
├─ AbstractValueObject.java
└─ exemplos/              # TaxRate, Discount, Price, InvoiceTotal
```

---

## ⚙️ 3. Implementações

### 3.1. CurrencyCode

```java
public final class CurrencyCode extends AbstractValueObject {

    private final Currency currency;

    private CurrencyCode(Currency currency) {
        this.currency = Objects.requireNonNull(currency, "currency must not be null");
    }

    public static CurrencyCode of(String code) {
        Objects.requireNonNull(code, "currency code must not be null");
        return new CurrencyCode(Currency.getInstance(code.toUpperCase()));
    }

    public static CurrencyCode of(Currency currency) {
        return new CurrencyCode(currency);
    }

    public String code() { return currency.getCurrencyCode(); }
    public int numericCode() { return currency.getNumericCode(); }
    public int defaultFractionDigits() { return currency.getDefaultFractionDigits(); }
    public Currency unwrap() { return currency; }

    @Override protected Object[] equalityComponents() { return new Object[]{ currency }; }
    @Override public String toString() { return code(); }
}
```

---

### 3.2. Money

```java
public final class Money extends AbstractValueObject {

    private final BigDecimal amount; // valor normalizado
    private final Currency currency;

    private static final int SCALE = 2; // ajuste padrão (pode ser por moeda)
    private static final RoundingMode ROUND = RoundingMode.HALF_EVEN;

    public Money(BigDecimal amount, Currency currency) {
        this.currency = req(currency, "currency");
        this.amount = req(amount, "amount").setScale(SCALE, ROUND).stripTrailingZeros();
    }

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }
    public static Money of(double amount, String currencyCode) {
        return new Money(BigDecimal.valueOf(amount), Currency.getInstance(currencyCode));
    }
    public static Money of(BigDecimal amount, String currencyCode) {
        return new Money(amount, Currency.getInstance(currencyCode));
    }

    public BigDecimal amount() { return amount; }
    public Currency currency() { return currency; }

    public Money plus(Money other) {
        ensureSameCurrency(other);
        return new Money(this.amount.add(other.amount), currency);
    }

    public Money minus(Money other) {
        ensureSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), currency);
    }

    public Money times(BigDecimal factor) {
        return new Money(this.amount.multiply(req(factor, "factor")), currency);
    }

    public Money allocate(int parts) {
        if (parts <= 0) throw new IllegalArgumentException("parts must be > 0");
        BigDecimal[] div = amount.divideAndRemainder(BigDecimal.valueOf(parts));
        return new Money(div[0], currency);
    }

    public int compareTo(Money other) {
        ensureSameCurrency(other);
        return this.amount.compareTo(other.amount);
    }

    private void ensureSameCurrency(Money other) {
        if (!this.currency.equals(req(other, "other").currency)) {
            throw new IllegalArgumentException("currency mismatch: %s vs %s"
                    .formatted(this.currency, other.currency));
        }
    }

    @Override protected Object[] equalityComponents() {
        return new Object[]{ amount, currency };
    }
}
```

---

## ✅ 4. Boas práticas incorporadas

- **Imutabilidade** → atributos finais e normalização no construtor.  
- **Validação forte** → moeda ISO-4217 garantida.  
- **Operações seguras** → proíbe operações entre moedas diferentes.  
- **Normalização** → valores ajustados para escala padrão (`SCALE=2`).  
- **Extensibilidade** → pode evoluir para suportar moedas com 0 ou 3 casas decimais.  

---

## 🧩 5. Exemplos práticos

### 5.1. Criando valores

```java
Money price = Money.of(200, "USD");
Money discount = Money.of(15, "USD");
```

### 5.2. Operações seguras

```java
Money total = price.minus(discount); // 185 USD
Money doubled = price.times(new BigDecimal("2")); // 400 USD
```

### 5.3. Comparação

```java
assert price.compareTo(discount) > 0;
```

### 5.4. Usando CurrencyCode

```java
CurrencyCode usd = CurrencyCode.of("USD");
System.out.println(usd.code()); // "USD"
System.out.println(usd.numericCode()); // 840
System.out.println(usd.defaultFractionDigits()); // 2
```

### 5.5. Integração com Domain Events

```java
public final class PaymentProcessed extends BaseDomainEvent {
    private final Money amount;
    private final CurrencyCode currency;

    public PaymentProcessed(Money amount, CurrencyCode currency) {
        super(null, Instant.now(), currency.code(), null, EventMetadata.minimal());
        this.amount = amount;
        this.currency = currency;
    }
}
```

---

## 🌍 6. Casos reais

- **E-commerce**: preços e descontos (`Money`).  
- **Billing**: faturas e pagamentos.  
- **Finance**: transações, portfólios.  
- **IoT**: medições monetárias (energia, consumo).  

---

## 🧪 7. Testes práticos

```java
@Test
void shouldAddMoneyCorrectly() {
    Money m1 = Money.of(100, "USD");
    Money m2 = Money.of(50, "USD");
    assertEquals(Money.of(150, "USD"), m1.plus(m2));
}

@Test
void shouldRejectDifferentCurrencies() {
    Money usd = Money.of(10, "USD");
    Money eur = Money.of(10, "EUR");
    assertThrows(IllegalArgumentException.class, () -> usd.plus(eur));
}
```

---

## ⚠️ 8. Erros comuns

❌ Usar `BigDecimal` cru em vez de VO → semântica perdida.  
❌ Misturar moedas diferentes em operações.  
❌ Não normalizar escala → comparações inconsistentes.  
❌ Usar `double` direto → perda de precisão.  

---

## 📌 9. Conclusão

Os **CurrencyCode & Money supremos** garantem:
- Representação clara e validada de moedas e valores monetários.  
- Imutabilidade e consistência em operações.  
- Proteção contra erros clássicos de finanças em software.  
- Integração natural com Entities, Aggregates e Domain Events.  

São **Value Objects essenciais** para qualquer domínio que lide com valores financeiros.  
