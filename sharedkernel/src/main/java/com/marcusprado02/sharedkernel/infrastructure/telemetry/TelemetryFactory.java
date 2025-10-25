package com.marcusprado02.sharedkernel.infrastructure.telemetry;


import java.net.URI;
import java.time.Duration;
import java.util.*;

public final class TelemetryFactory {
    public static TelemetryExporter createFromEnv() {
        var env = System.getenv();
        TelemetryConfig cfg = TelemetryConfig.builder()
            .service(
                env.getOrDefault("OTEL_SERVICE_NAME", "app"),
                env.getOrDefault("OTEL_SERVICE_VERSION","0.0.1"),
                env.getOrDefault("OTEL_ENV","dev"))
            .resourceAttributes(parseKv(env.getOrDefault("OTEL_RESOURCE_ATTRS","deployment.environment="+env.getOrDefault("OTEL_ENV","dev"))))
            .traces(parseKind(env.getOrDefault("OTEL_TRACES_EXPORT","OTLP")))
            .metrics(parseKind(env.getOrDefault("OTEL_METRICS_EXPORT","PROMETHEUS")))
            .logs(parseKind(env.getOrDefault("OTEL_LOGS_EXPORT","NONE")))
            .otlp(parseProto(env.getOrDefault("OTEL_EXPORTER_OTLP_PROTOCOL","HTTP")),
                  URI.create(env.getOrDefault("OTEL_EXPORTER_OTLP_ENDPOINT","http://localhost:4318")))
            .exportInterval(Duration.ofSeconds(Integer.parseInt(env.getOrDefault("OTEL_EXPORT_INTERVAL_SEC","5"))))
            .tracesSamplerRatio(Double.parseDouble(env.getOrDefault("OTEL_TRACES_SAMPLER_RATIO","1.0")))
            .enableRuntimeMetrics(Boolean.parseBoolean(env.getOrDefault("OTEL_RUNTIME_METRICS","true")))
            .enableMicrometerBridge(Boolean.parseBoolean(env.getOrDefault("OTEL_MICROMETER_BRIDGE","true")))
            .enableLogCorrelation(Boolean.parseBoolean(env.getOrDefault("OTEL_LOG_CORRELATION","true")))
            .additionalPropagators(Set.of(env.getOrDefault("OTEL_PROPAGATORS_EXTRA","").split(",")))
            .headers(parseKv(env.getOrDefault("OTEL_EXPORTER_OTLP_HEADERS","")))
            .build();
        return new DefaultTelemetryExporter(cfg);
    }

    private static TelemetryConfig.ExportKind parseKind(String s){
        try { return TelemetryConfig.ExportKind.valueOf(s.trim().toUpperCase(Locale.ROOT)); }
        catch (Exception e){ return TelemetryConfig.ExportKind.NONE; }
    }
    private static TelemetryConfig.Protocol parseProto(String s){
        return "GRPC".equalsIgnoreCase(s)? TelemetryConfig.Protocol.GRPC : TelemetryConfig.Protocol.HTTP;
    }
    private static Map<String,String> parseKv(String csv){
        if (csv==null || csv.isBlank()) return Map.of();
        Map<String,String> m = new LinkedHashMap<>();
        for (String kv : csv.split(",")) {
            if (kv.isBlank() || !kv.contains("=")) continue;
            var p = kv.split("=",2); m.put(p[0].trim(), p[1].trim());
        }
        return m;
    }
}