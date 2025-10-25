package com.marcusprado02.sharedkernel.infrastructure.sms.spi;


import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/** Configurações do provider (chaves, endpoints, flags). Normalmente vem de Secrets/Vault. */
public interface ProviderConfig {

    /** Lê uma chave como string; retorna empty se não existir. */
    Optional<String> get(String key);

    /** Lê uma chave obrigatória (lança IllegalArgumentException se ausente). */
    default String require(String key) {
        return get(key).orElseThrow(() -> new IllegalArgumentException("Missing config: " + key));
    }

    /** Atalhos convenientes. */
    default int getInt(String key, int def) {
        return get(key).map(Integer::parseInt).orElse(def);
    }
    default boolean getBool(String key, boolean def) {
        return get(key).map(v -> "true".equalsIgnoreCase(v) || "1".equals(v)).orElse(def);
    }

    /** Exposição como mapa/props (útil para libs externas). */
    Map<String,String> asMap();
    default Properties asProperties() {
        Properties p = new Properties();
        asMap().forEach(p::setProperty);
        return p;
    }

    /** Id lógico do provider (ex.: "twilio", "sns"). */
    String providerId();
}