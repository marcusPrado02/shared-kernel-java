package com.marcusprado02.sharedkernel.infrastructure.secrets;

import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public record SecretRef(
        String path,               // ex.: "db/primary/password" ou "kv/payments/apiKey"
        String environment,        // dev|staging|prod
        String tenant,             // tenant id (opcional)
        Map<String,String> labels  // labels extras (service=orders, region=sa-east-1)
) {
    public static SecretRef of(String path){ return new SecretRef(path, null, null, Map.of()); }
    public SecretRef withEnv(String env){ return new SecretRef(path, env, tenant, labels); }
    public SecretRef withTenant(String t){ return new SecretRef(path, environment, t, labels); }
    public SecretRef withLabels(Map<String,String> l){ return new SecretRef(path, environment, tenant, l); }
    public String fqn() {
        var sb = new StringBuilder();
        if (tenant!=null) sb.append("tenants/").append(tenant).append("/");
        if (environment!=null) sb.append(environment).append("/");
        sb.append(path);
        return sb.toString();
    }
}
