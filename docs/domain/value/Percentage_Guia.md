# 📘 Guia Definitivo do **Percentage Supremo**

## 🔑 1. Conceito

O **Percentage** é um **Value Object** que representa porcentagens de forma segura e imutável, armazenadas como frações no intervalo `[0,1]`.  
Ele substitui o uso inseguro de `double`/`BigDecimal` crus em cálculos de percentual, evitando erros de arredondamento e inconsistência de escala.

### Benefícios
- **Imutabilidade**.  
- **Validação forte** → sempre dentro de `[0,1]`.  
- **Conversão clara** → fração ↔ percentual.  
- **Operações expressivas** → `applyTo(base)`.  

---

## 🏗️ 2. Estrutura no Shared-Kernel

```
com.yourorg.sharedkernel.domain.model.value.measure
├─ Percentage.java           # VO para porcentagens
├─ AbstractValueObject.java  # base para igualdade por valor
└─ exemplos/                 # TaxRate, Discount, Allocation
```

---

## ⚙️ 3. Implementação

```java
public final class Percentage extends AbstractValueObject {
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE = BigDecimal.ONE;

    private final BigDecimal value; // canônico: 0.0 .. 1.0

    private Percentage(BigDecimal v) {
        if (v.compareTo(ZERO) < 0 || v.compareTo(ONE) > 0) {
            throw new IllegalArgumentException("percentage must be in [0,1]");
        }
        this.value = v.stripTrailingZeros();
    }

    public static Percentage ofFraction(BigDecimal fraction) { return new Percentage(fraction); }

    public static Percentage ofPercent(double percent) { // 0..100
        return new Percentage(BigDecimal.valueOf(percent)
            .divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_EVEN));
    }

    public BigDecimal asFraction() { return value; }
    public BigDecimal asPercent() { return value.multiply(BigDecimal.valueOf(100)); }

    public BigDecimal applyTo(BigDecimal base) {
        return base.multiply(value);
    }

    @Override protected Object[] equalityComponents() { return new Object[]{ value }; }
}
```

---

## ✅ 4. Boas práticas incorporadas

- Representa porcentagem **sempre como fração** (`0.25` = 25%).  
- Criação via fábricas (`ofFraction`, `ofPercent`).  
- Usa `RoundingMode.HALF_EVEN` para evitar viés em cálculos repetidos.  
- Imutável → não há setters.  

---

## 🧩 5. Exemplos práticos

### 5.1. Criando porcentagens

```java
Percentage p1 = Percentage.ofFraction(new BigDecimal("0.25")); // 25%
Percentage p2 = Percentage.ofPercent(40); // 40%
```

---

### 5.2. Convertendo

```java
Percentage p = Percentage.ofPercent(12.5);
System.out.println(p.asFraction()); // 0.125
System.out.println(p.asPercent());  // 12.5
```

---

### 5.3. Aplicando sobre valores

```java
Percentage discount = Percentage.ofPercent(15);
BigDecimal price = new BigDecimal("200");
BigDecimal discounted = price.subtract(discount.applyTo(price)); // 170
```

---

### 5.4. Usando em domínio

```java
public final class TaxRate extends AbstractValueObject {
    private final Percentage rate;

    public TaxRate(Percentage rate) {
        this.rate = rate;
    }

    public BigDecimal apply(BigDecimal base) {
        return rate.applyTo(base);
    }

    @Override protected Object[] equalityComponents() { return new Object[]{ rate }; }
}
```

---

## 🌍 6. Casos reais

- **E-commerce**: desconto em `Order` (`Discount = Percentage`).  
- **Billing**: cálculo de `TaxRate`.  
- **Finance**: alocação de `Portfolio` por `Percentage`.  
- **IoT**: medição de nível de bateria (0–100%).  

---

## 🧪 7. Testes práticos

```java
@Test
void shouldCreatePercentageFromFraction() {
    var p = Percentage.ofFraction(new BigDecimal("0.3"));
    assertEquals(new BigDecimal("0.3"), p.asFraction());
}

@Test
void shouldRejectInvalidPercent() {
    assertThrows(IllegalArgumentException.class, () -> Percentage.ofFraction(new BigDecimal("1.5")));
}

@Test
void shouldApplyPercentageCorrectly() {
    var p = Percentage.ofPercent(20);
    assertEquals(new BigDecimal("200").multiply(new BigDecimal("0.2")), p.applyTo(new BigDecimal("200")));
}
```

---

## ⚠️ 8. Erros comuns

❌ Usar `double` cru para representar porcentagem (perda de precisão).  
❌ Não validar limites (aceitar valores fora de `[0,1]`).  
❌ Misturar representação em fração/percentual sem conversão clara.  
❌ Arredondar de forma inconsistente → divergências em relatórios financeiros.  

---

## 📌 9. Conclusão

O **Percentage supremo** garante:  
- Representação clara e validada de porcentagens.  
- Imutabilidade e precisão.  
- Operações seguras e semânticas (`applyTo`).  
- Integração natural com cálculos financeiros, tributários e estatísticos.  

É um **Value Object essencial** para modelagem quantitativa em domínios críticos.  
