# 📘 Guia Definitivo do **SemVer Supremo**

## 🔑 1. Conceito

O **SemVer** é um **Value Object** que implementa o padrão [Semantic Versioning 2.0.0](https://semver.org/).  
Ele fornece uma representação forte, imutável e validada para versões de software, garantindo comparações corretas e semântica clara.

Formato oficial:
```
MAJOR.MINOR.PATCH[-PRERELEASE][+BUILD]
```

### Benefícios
- **Imutável e validado** via regex.  
- **Comparável** → implementa `Comparable<SemVer>`.  
- **Expressivo** → separa partes da versão (`major`, `minor`, `patch`).  
- **Integrável** → usado em APIs, pacotes, eventos de schema.  

---

## 🏗️ 2. Estrutura no Shared-Kernel

```
com.yourorg.sharedkernel.domain.model.value.versioning
├─ SemVer.java              # VO de versão semântica
├─ AbstractValueObject.java # base de igualdade por valor
└─ exemplos/                # SchemaVersion, APIVersion, ReleaseTag
```

---

## ⚙️ 3. Implementação

```java
public final class SemVer extends AbstractValueObject implements Comparable<SemVer> {
    private static final Pattern RE = Pattern.compile(
        "^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)" +
        "(?:-([0-9A-Za-z-\.]+))?(?:\+([0-9A-Za-z-\.]+))?$"
    );

    private final int major, minor, patch;
    private final String preRelease;  // opcional
    private final String buildMeta;   // opcional

    private SemVer(int major, int minor, int patch, String preRelease, String buildMeta) {
        this.major = major; this.minor = minor; this.patch = patch;
        this.preRelease = preRelease; this.buildMeta = buildMeta;
    }

    public static SemVer parse(String s) {
        var m = RE.matcher(Objects.requireNonNull(s, "null semver"));
        if (!m.matches()) throw new IllegalArgumentException("invalid semver: " + s);
        return new SemVer(
            Integer.parseInt(m.group(1)),
            Integer.parseInt(m.group(2)),
            Integer.parseInt(m.group(3)),
            m.group(4), m.group(5)
        );
    }

    public String value() {
        return "%d.%d.%d%s%s".formatted(
            major, minor, patch,
            preRelease != null ? "-" + preRelease : "",
            buildMeta  != null ? "+" + buildMeta  : ""
        );
    }

    @Override protected Object[] equalityComponents() {
        return new Object[]{ major, minor, patch, preRelease, buildMeta };
    }

    @Override public int compareTo(SemVer o) {
        if (major != o.major) return Integer.compare(major, o.major);
        if (minor != o.minor) return Integer.compare(minor, o.minor);
        if (patch != o.patch) return Integer.compare(patch, o.patch);
        if (preRelease == null && o.preRelease != null) return 1;
        if (preRelease != null && o.preRelease == null) return -1;
        if (preRelease == null) return 0;
        return preRelease.compareTo(o.preRelease);
    }
}
```

---

## ✅ 4. Boas práticas incorporadas

- **Construtor privado + parse()** → garante formato válido.  
- **Regex oficial** → cobre todos os casos do SemVer 2.0.0.  
- **Comparable** → ordena corretamente versões (com ou sem `preRelease`).  
- **Semântica explícita** → separação clara de partes da versão.  

---

## 🧩 5. Exemplos práticos

### 5.1. Parseando versões

```java
SemVer v1 = SemVer.parse("1.2.3");
SemVer v2 = SemVer.parse("2.0.0-alpha+001");

System.out.println(v1.value()); // "1.2.3"
System.out.println(v2.value()); // "2.0.0-alpha+001"
```

### 5.2. Comparando versões

```java
SemVer v1 = SemVer.parse("1.0.0");
SemVer v2 = SemVer.parse("2.0.0");

assert v1.compareTo(v2) < 0;
```

### 5.3. Comparando pre-releases

```java
SemVer alpha = SemVer.parse("1.0.0-alpha");
SemVer beta = SemVer.parse("1.0.0-beta");
SemVer release = SemVer.parse("1.0.0");

assert alpha.compareTo(beta) < 0;
assert beta.compareTo(release) < 0;
```

### 5.4. Em eventos de schema

```java
public final class SchemaVersionedEvent extends BaseDomainEvent {
    private final SemVer schemaVersion;

    public SchemaVersionedEvent(SemVer schemaVersion) {
        super(null, Instant.now(), "tenant-1", null, EventMetadata.minimal());
        this.schemaVersion = schemaVersion;
    }

    @Override public String eventType() { return "schema.changed"; }
    @Override public int schemaVersion() { return schemaVersion.major(); }
}
```

---

## 🌍 6. Casos reais

- **API Versioning** → contratos REST/GraphQL evolutivos.  
- **Schema Migration** → versionar eventos/event stores.  
- **Pacotes de software** → dependências (`lib-1.2.3`).  
- **IoT** → versionar firmwares.  

---

## 🧪 7. Testes práticos

```java
@Test
void shouldParseValidVersion() {
    var v = SemVer.parse("1.2.3-alpha+exp");
    assertEquals("1.2.3-alpha+exp", v.value());
}

@Test
void shouldRejectInvalidVersion() {
    assertThrows(IllegalArgumentException.class, () -> SemVer.parse("1.2"));
}

@Test
void shouldCompareVersionsCorrectly() {
    var v1 = SemVer.parse("1.0.0-alpha");
    var v2 = SemVer.parse("1.0.0");
    assertTrue(v1.compareTo(v2) < 0);
}
```

---

## ⚠️ 8. Erros comuns

❌ Usar `String` crua para versionamento.  
❌ Ignorar pre-release e build metadata na comparação.  
❌ Não validar versões antes de persistir.  
❌ Tratar versão como `double` (e.g., 1.2 → 1.19999).  

---

## 📌 9. Conclusão

O **SemVer supremo** garante:  
- Representação forte e validada de versões semânticas.  
- Comparação correta e ordenação previsível.  
- Uso consistente em APIs, schemas e eventos.  
- Aderência ao padrão oficial **Semantic Versioning 2.0.0**.  

É um **Value Object essencial** para evolutividade e governança em sistemas modernos.  
