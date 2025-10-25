package com.marcusprado02.sharedkernel.infrastructure.secrets;

public record SecretId(
        String namespace,    // ex.: "payments/prod" ou "kv/app"
        String name,         // ex.: "db-password"
        String version,      // ex.: "42", "latest", "AWSCURRENT", "previous", null
        String stage         // label/estágio opcional (ex.: AWSCURRENT/AWSPREVIOUS; "current","previous")
) {
    public static SecretId of(String ns, String name){ return new SecretId(ns, name, null, null); }
}
