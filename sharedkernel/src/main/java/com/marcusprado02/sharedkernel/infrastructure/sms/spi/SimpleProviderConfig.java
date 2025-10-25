package com.marcusprado02.sharedkernel.infrastructure.sms.spi;


import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class SimpleProviderConfig implements ProviderConfig {
    private final String providerId;
    private final Map<String,String> data;
    public SimpleProviderConfig(String providerId, Map<String,String> data) {
        this.providerId = providerId;
        this.data = data == null ? Map.of() : Map.copyOf(data);
    }
    @Override public Optional<String> get(String key) { return Optional.ofNullable(data.get(key)); }
    @Override public Map<String, String> asMap() { return Collections.unmodifiableMap(data); }
    @Override public String providerId() { return providerId; }
}
