# 📘 Guia Definitivo do **GeoHash Supremo**

## 🔑 1. Conceito

O **GeoHash** é um **Value Object** usado para representar **localizações geográficas em forma compacta**.  
Ele converte latitude/longitude em uma string baseada em um **alfabeto de 32 caracteres**, que preserva proximidade espacial: geohashes semelhantes representam regiões próximas.

### Benefícios
- **Compactação** → coordenadas reduzidas a uma string curta.  
- **Ordenação** → geohashes próximos ordenam juntos lexicograficamente.  
- **Indexação geoespacial** → ideal para bancos como Elasticsearch, Cassandra, Redis.  
- **Hierarquia** → encurtar o geohash gera "áreas pai".  

---

## 🏗️ 2. Estrutura no Shared-Kernel

```
com.yourorg.sharedkernel.domain.model.value.geo
├─ GeoHash.java             # VO imutável para representar localizações
├─ AbstractValueObject.java # base para igualdade por valor
└─ exemplos/                # uso em DeviceLocation, Address, Region
```

---

## ⚙️ 3. Implementação

```java
public final class GeoHash extends AbstractValueObject {
    private static final String ALPHABET = "0123456789bcdefghjkmnpqrstuvwxyz";
    private final String value;

    private GeoHash(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("geohash blank");
        for (char c : value.toLowerCase().toCharArray()) {
            if (ALPHABET.indexOf(c) < 0) throw new IllegalArgumentException("invalid geohash char: " + c);
        }
        this.value = value.toLowerCase();
    }

    public static GeoHash of(String value) { return new GeoHash(value); }
    public String value() { return value; }

    public GeoHash parent() {
        if (value.length() <= 1) throw new IllegalStateException("no parent");
        return new GeoHash(value.substring(0, value.length() - 1));
    }

    @Override protected Object[] equalityComponents() { return new Object[]{ value }; }
}
```

---

## ✅ 4. Boas práticas incorporadas

- **Imutável** → não permite alterar `value`.  
- **Validação forte** → caracteres inválidos rejeitados.  
- **Normalização** → sempre minúsculo (`toLowerCase()`).  
- **Hierarquia natural** → `parent()` retorna regiões maiores.  

---

## 🧩 5. Exemplos práticos

### 5.1. Criando um GeoHash

```java
GeoHash g = GeoHash.of("6gkzwgjzn820");
System.out.println(g.value()); // "6gkzwgjzn820"
```

---

### 5.2. Obtendo região pai

```java
GeoHash g = GeoHash.of("6gkzwgjzn820");
GeoHash parent = g.parent();
System.out.println(parent.value()); // "6gkzwgjzn82"
```

---

### 5.3. Em DeviceLocation

```java
public final class DeviceLocation extends AbstractValueObject {
    private final GeoHash geohash;
    private final Instant recordedAt;

    public DeviceLocation(GeoHash geohash, Instant recordedAt) {
        this.geohash = geohash;
        this.recordedAt = recordedAt;
    }

    @Override protected Object[] equalityComponents() {
        return new Object[]{ geohash, recordedAt };
    }

    public GeoHash geohash() { return geohash; }
    public Instant recordedAt() { return recordedAt; }
}
```

---

### 5.4. Em consultas (Elasticsearch, Redis)

- **Prefix search**: `geoHash.startsWith("6gkz")` → encontra pontos próximos.  
- **Hierarquia**: truncar geohash = consultar área maior.  

---

## 🌍 6. Casos reais

- **IoT**: indexar leituras de sensores por região.  
- **Logística**: rastrear veículos em tempo real.  
- **Social**: feed de eventos baseado em proximidade.  
- **E-commerce**: busca de produtos/lojas próximos.  

---

## 🧪 7. Testes práticos

```java
@Test
void shouldCreateValidGeoHash() {
    GeoHash g = GeoHash.of("6gkzwgjzn820");
    assertEquals("6gkzwgjzn820", g.value());
}

@Test
void shouldRejectInvalidChar() {
    assertThrows(IllegalArgumentException.class, () -> GeoHash.of("XYZ123"));
}

@Test
void parentShouldReturnOneLevelUp() {
    GeoHash g = GeoHash.of("6gkzwgjzn820");
    assertEquals("6gkzwgjzn82", g.parent().value());
}
```

---

## ⚠️ 8. Erros comuns

❌ Usar `String` crua em vez de VO → sem validação, risco de dados inválidos.  
❌ Assumir que geohash é latitude/longitude exata (ele é aproximado por célula).  
❌ Ignorar hierarquia → consultas ineficientes.  
❌ Não normalizar para minúsculo.  

---

## 📌 9. Conclusão

O **GeoHash supremo** garante:
- Representação compacta e validada de localização.  
- Integração natural com bancos e buscas geoespaciais.  
- Operações hierárquicas úteis (`parent`).  
- Segurança semântica ao substituir `String` por VO.  

É um **Value Object essencial** para modelagem de localização em sistemas modernos.  
