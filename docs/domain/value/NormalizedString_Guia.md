# 📘 Guia Definitivo do **NormalizedString Supremo**

## 🔑 1. Conceito

O **NormalizedString** é um **Value Object** que encapsula a estratégia de **normalização de strings** para garantir consistência, imutabilidade e igualdade semântica em dados textuais.  

Ele resolve problemas comuns como:
- Diferenças de maiúsculas/minúsculas.  
- Presença de espaços em excesso.  
- Caracteres com acentos/diacríticos.  
- Comparação textual inconsistente.  

---

## 🏗️ 2. Estrutura no Shared-Kernel

```
com.yourorg.sharedkernel.domain.model.value.normalization
├─ NormalizedString.java      # VO para strings normalizadas
├─ AbstractValueObject.java   # base de igualdade por valor
└─ exemplos/                  # Email, Username, SearchKey
```

---

## ⚙️ 3. Implementação

```java
public final class NormalizedString extends AbstractValueObject {

    private final String value;

    private NormalizedString(String normalized) {
        this.value = Objects.requireNonNull(normalized, "normalized must not be null");
    }

    public static NormalizedString of(String raw, Function<String,String> strategy) {
        Objects.requireNonNull(strategy, "strategy must not be null");
        return new NormalizedString(strategy.apply(Objects.requireNonNull(raw, "raw must not be null")));
    }

    public static NormalizedString lowerTrimmed(String raw) {
        return new NormalizedString(raw == null ? null : raw.trim().toLowerCase(Locale.ROOT));
    }

    public static NormalizedString noDiacritics(String raw) {
        if (raw == null) throw new IllegalArgumentException("raw must not be null");
        var norm = Normalizer.normalize(raw, Form.NFD)
                             .replaceAll("\p{M}", "")
                             .toLowerCase(Locale.ROOT)
                             .trim();
        return new NormalizedString(norm);
    }

    public String value() { return value; }

    @Override protected Object[] equalityComponents() { return new Object[]{ value }; }

    @Override public String toString() { return value; }
}
```

---

## ✅ 4. Boas práticas incorporadas

- **Imutabilidade** → valor é final e definido apenas no construtor.  
- **Validação forte** → `null` proibido, exceto via estratégias explícitas.  
- **Estratégias flexíveis** → `of(raw, strategy)` permite pluggabilidade.  
- **Semântica explícita** → nomes expressivos (`lowerTrimmed`, `noDiacritics`).  

---

## 🧩 5. Exemplos práticos

### 5.1. Normalização simples

```java
NormalizedString s = NormalizedString.lowerTrimmed(" João ");
System.out.println(s.value()); // "joão"
```

### 5.2. Removendo acentos

```java
NormalizedString s = NormalizedString.noDiacritics("João Ávila");
System.out.println(s.value()); // "joao avila"
```

### 5.3. Estratégia customizada

```java
NormalizedString s = NormalizedString.of("ABC-123", r -> r.replace("-", "").toLowerCase());
System.out.println(s.value()); // "abc123"
```

### 5.4. Em VO de domínio

```java
public final class Email extends AbstractValueObject {
    private final NormalizedString address;

    public Email(String raw) {
        this.address = NormalizedString.lowerTrimmed(raw);
        if (!address.value().contains("@")) throw new IllegalArgumentException("invalid email");
    }

    @Override protected Object[] equalityComponents() { return new Object[]{ address }; }

    public String value() { return address.value(); }
}
```

---

## 🌍 6. Casos reais

- **Usuários**: `Username`, `Email`.  
- **E-commerce**: normalizar `SKU` ou `ProductCode`.  
- **IoT**: padronizar nomes de sensores.  
- **Search/Index**: chaves de busca insensíveis a acentos/maiúsculas.  

---

## 🧪 7. Testes práticos

```java
@Test
void shouldNormalizeToLowerAndTrim() {
    var n = NormalizedString.lowerTrimmed(" João ");
    assertEquals("joão", n.value());
}

@Test
void shouldRemoveDiacritics() {
    var n = NormalizedString.noDiacritics("Árvore São");
    assertEquals("arvore sao", n.value());
}

@Test
void shouldAllowCustomStrategy() {
    var n = NormalizedString.of("ABC-123", s -> s.replace("-", ""));
    assertEquals("ABC123", n.value());
}
```

---

## ⚠️ 8. Erros comuns

❌ Usar `String` crua sem normalização → inconsistências em queries/comparações.  
❌ Esquecer de validar `null` ou string vazia.  
❌ Misturar diferentes estratégias sem documentar → resultados inconsistentes.  
❌ Não encapsular em VO → lógica de normalização espalhada.  

---

## 📌 9. Conclusão

O **NormalizedString supremo** garante:  
- Consistência semântica em dados textuais.  
- Estratégias flexíveis para normalização.  
- Proteção contra erros comuns em comparações de string.  
- Integração natural com VOs e entidades de domínio.  

É um **Value Object essencial** para domínios que dependem de dados textuais limpos e consistentes.  
