package com.marcusprado02.sharedkernel.infrastructure.telemetry;


import java.net.URI;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class TelemetryConfig {
    public enum ExportKind { NONE, STDOUT, OTLP, PROMETHEUS, DATADOG, NEW_RELIC, CLOUDWATCH, LOKI_OTLP }
    public enum Protocol { GRPC, HTTP }

    public final String serviceName;
    public final String serviceVersion;
    public final String serviceEnv;         // dev/stage/prod
    public final Map<String,String> resourceAttributes; // region, cluster, namespace, etc.
    public final ExportKind traces;
    public final ExportKind metrics;
    public final ExportKind logs;
    public final Protocol otlpProtocol;
    public final URI otlpEndpoint;          // ex: http://otel-collector:4318
    public final Duration exportInterval;   // metrics/logs batching
    public final double tracesSamplerRatio; // 0..1
    public final boolean enableRuntimeMetrics; // JDK, process, host
    public final boolean enableMicrometerBridge;
    public final boolean enableLogCorrelation; // trace/log correlation
    public final Set<String> additionalPropagators; // ex: b3, b3multi, xray
    public final Map<String,String> headers; // OTLP auth headers

    private TelemetryConfig(Builder b) { /* copiar campos */ 
        this.serviceName=b.serviceName; this.serviceVersion=b.serviceVersion; this.serviceEnv=b.serviceEnv;
        this.resourceAttributes=b.resourceAttributes; this.traces=b.traces; this.metrics=b.metrics; this.logs=b.logs;
        this.otlpProtocol=b.otlpProtocol; this.otlpEndpoint=b.otlpEndpoint; this.exportInterval=b.exportInterval;
        this.tracesSamplerRatio=b.tracesSamplerRatio; this.enableRuntimeMetrics=b.enableRuntimeMetrics;
        this.enableMicrometerBridge=b.enableMicrometerBridge; this.enableLogCorrelation=b.enableLogCorrelation;
        this.additionalPropagators=b.additionalPropagators; this.headers=b.headers;
    }

    public static Builder builder(){ return new Builder(); }
    public static final class Builder {
        private String serviceName="app";
        private String serviceVersion="0.0.1";
        private String serviceEnv="dev";
        private Map<String,String> resourceAttributes = Map.of();
        private ExportKind traces=ExportKind.OTLP;
        private ExportKind metrics=ExportKind.PROMETHEUS; // padrão comum: Prometheus pull
        private ExportKind logs=ExportKind.NONE;
        private Protocol otlpProtocol=Protocol.GRPC;
        private URI otlpEndpoint=URI.create("http://localhost:4318");
        private Duration exportInterval=Duration.ofSeconds(5);
        private double tracesSamplerRatio=1.0;
        private boolean enableRuntimeMetrics=true;
        private boolean enableMicrometerBridge=true;
        private boolean enableLogCorrelation=true;
        private Set<String> additionalPropagators = Set.of(); // ex: "b3"
        private Map<String,String> headers = Map.of();

        // setters fluentes...
        public Builder service(String name, String version, String env){ this.serviceName=name; this.serviceVersion=version; this.serviceEnv=env; return this; }
        public Builder resourceAttributes(Map<String,String> m){ this.resourceAttributes=m; return this; }
        public Builder traces(ExportKind k){ this.traces=k; return this; }
        public Builder metrics(ExportKind k){ this.metrics=k; return this; }
        public Builder logs(ExportKind k){ this.logs=k; return this; }
        public Builder otlp(Protocol p, URI endpoint){ this.otlpProtocol=p; this.otlpEndpoint=endpoint; return this; }
        public Builder exportInterval(Duration d){ this.exportInterval=d; return this; }
        public Builder tracesSamplerRatio(double r){ this.tracesSamplerRatio=r; return this; }
        public Builder enableRuntimeMetrics(boolean v){ this.enableRuntimeMetrics=v; return this; }
        public Builder enableMicrometerBridge(boolean v){ this.enableMicrometerBridge=v; return this; }
        public Builder enableLogCorrelation(boolean v){ this.enableLogCorrelation=v; return this; }
        public Builder additionalPropagators(Set<String> p){ this.additionalPropagators=p; return this; }
        public Builder headers(Map<String,String> h){ this.headers=h; return this; }
        public TelemetryConfig build(){ return new TelemetryConfig(this); }
    }
}

