package com.marcusprado02.sharedkernel.infrastructure.maptile.spi;


import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public interface ProviderConfig {
    Optional<String> get(String key);

    default String require(String key) {
        return get(key).orElseThrow(() -> new IllegalArgumentException("Missing config: " + key));
    }

    /** Conveniência comum para XYZ HTTP templates: ex. https://host/{z}/{x}/{y}{r}.png */
    default String template() { return require("template"); }

    Map<String,String> asMap();

    default Properties asProperties() {
        Properties p = new Properties();
        asMap().forEach(p::setProperty);
        return p;
    }

    String providerId();
}