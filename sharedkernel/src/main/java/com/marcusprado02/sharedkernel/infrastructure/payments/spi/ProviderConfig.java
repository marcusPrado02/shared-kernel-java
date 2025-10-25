package com.marcusprado02.sharedkernel.infrastructure.payments.spi;


import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/** Configurações do provider de pagamentos (chaves, endpoints, flags). */
public interface ProviderConfig {

    /** Lê uma chave como string; empty se ausente. */
    Optional<String> get(String key);

    /** Lê uma chave obrigatória; lança IllegalArgumentException se ausente. */
    default String require(String key) {
        return get(key).orElseThrow(() -> new IllegalArgumentException("Missing config: " + key));
    }

    /** Conveniências. */
    default int getInt(String key, int def) {
        return get(key).map(Integer::parseInt).orElse(def);
    }
    default boolean getBool(String key, boolean def) {
        return get(key).map(v -> "true".equalsIgnoreCase(v) || "1".equals(v)).orElse(def);
    }

    /** Mapa bruto das configs (útil para SDKs). */
    Map<String, String> asMap();

    default Properties asProperties() {
        Properties p = new Properties();
        asMap().forEach(p::setProperty);
        return p;
    }

    /** Id lógico do provider, ex.: "stripe", "adyen", "pagarme". */
    String providerId();
}