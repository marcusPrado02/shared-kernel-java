package com.marcusprado02.sharedkernel.infrastructure.telemetry;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;

import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.logging.SystemOutLogRecordExporter;

import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;

import io.opentelemetry.extension.trace.propagation.B3Propagator;
// X-Ray é opcional; se não tiver a dependência, comente a linha abaixo e o trecho no buildPropagators
import io.opentelemetry.contrib.awsxray.propagator.AwsXrayPropagator;

import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.CompletableResultCode;

import io.opentelemetry.sdk.logs.LogLimits;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;

import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;

import io.opentelemetry.sdk.resources.Resource;

import io.opentelemetry.sdk.trace.IdGenerator;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;

import java.lang.reflect.Constructor;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Implementação robusta do TelemetryExporter.
 * - Sem dependência obrigatória de micrometer-prometheus e runtime-metrics.
 * - Se presentes no classpath, são ativados automaticamente.
 */
public final class DefaultTelemetryExporter implements TelemetryExporter {

    private final OpenTelemetrySdk sdk;
    private final SdkTracerProvider tracerProvider;
    private final SdkMeterProvider meterProvider;
    private final SdkLoggerProvider loggerProvider;
    private final MeterRegistry micrometerRegistry;
    private final TelemetryConfig cfg;

    public DefaultTelemetryExporter(TelemetryConfig cfg) {
        this.cfg = cfg;

        // -------- Resource --------
        AttributesBuilder ab = Attributes.builder()
            .put("service.name", cfg.serviceName)
            .put("service.version", cfg.serviceVersion)
            .put("deployment.environment", cfg.serviceEnv);
        if (cfg.resourceAttributes != null) {
            for (Map.Entry<String, String> e : cfg.resourceAttributes.entrySet()) {
                if (e.getKey() != null && e.getValue() != null) {
                    ab.put(e.getKey(), e.getValue());
                }
            }
        }
        Resource resource = Resource.getDefault().merge(Resource.create(ab.build()));

        // -------- TracerProvider --------
        SpanExporter spanExporter = switch (cfg.traces) {
            case NONE ->  new NoopSpanExporter();
            case STDOUT -> LoggingSpanExporter.create();
            case OTLP -> buildOtlpSpanExporter(cfg);
            case DATADOG, NEW_RELIC, CLOUDWATCH, LOKI_OTLP, PROMETHEUS -> buildOtlpSpanExporter(cfg); // via collector
        };

        BatchSpanProcessor bsp = BatchSpanProcessor.builder(spanExporter)
            .setScheduleDelay(cfg.exportInterval)
            .setExporterTimeout(Duration.ofSeconds(15))
            .setMaxQueueSize(8192)
            .setMaxExportBatchSize(512)
            .build();

        tracerProvider = SdkTracerProvider.builder()
            .setResource(resource)
            .setSampler(Sampler.traceIdRatioBased(cfg.tracesSamplerRatio))
            .addSpanProcessor(bsp)
            .setIdGenerator(IdGenerator.random())
            .build();

        // -------- MeterProvider --------
        SdkMeterProviderBuilder meterBuilder = SdkMeterProvider.builder().setResource(resource);

        boolean registerMetricReader = false;
        MetricExporter metricExporter = null;

        switch (cfg.metrics) {
            case NONE -> {
                // Sem exporter OTel. (Micrometer pode expor separadamente, se quiser)
            }
            case PROMETHEUS -> {
                // Exposição via Micrometer (/actuator/prometheus). Sem reader/exporter OTel.
            }
            case OTLP, DATADOG, NEW_RELIC, CLOUDWATCH, LOKI_OTLP -> {
                metricExporter = buildOtlpMetricExporter(cfg);
                registerMetricReader = true;
            }
            case STDOUT -> {
                // Não há SystemOutMetricExporter oficial estável — ignorar.
            }
        }

        if (registerMetricReader && metricExporter != null) {
            var metricReader = PeriodicMetricReader.builder(metricExporter)
                .setInterval(cfg.exportInterval)
                .build();
            meterBuilder.registerMetricReader(metricReader);
        }

        meterProvider = meterBuilder.build();

        // -------- Runtime Metrics (opcional via reflection) --------
        // Se a lib "opentelemetry-runtime-metrics" estiver no classpath:
        tryEnableRuntimeMetrics(meterProvider, cfg.enableRuntimeMetrics);

        // -------- LoggerProvider --------
        LogRecordExporter logExporter = switch (cfg.logs) {
            case NONE -> new NoopLogRecordExporter();
            case STDOUT -> SystemOutLogRecordExporter.create();
            case OTLP, LOKI_OTLP -> buildOtlpLogExporter(cfg);
            case DATADOG, NEW_RELIC, CLOUDWATCH, PROMETHEUS -> buildOtlpLogExporter(cfg); // via collector
        };

        var logProcessor = BatchLogRecordProcessor.builder(logExporter)
            .setScheduleDelay(cfg.exportInterval)
            .setMaxQueueSize(8192)
            .setExporterTimeout(Duration.ofSeconds(15))
            .build();

        loggerProvider = SdkLoggerProvider.builder()
            .setResource(resource)
            .setLogLimits(() -> LogLimits.getDefault()) // Supplier<LogLimits>
            .addLogRecordProcessor(logProcessor)
            .build();

        // -------- Propagators --------
        TextMapPropagator globalPropagator = buildPropagators(cfg);

        // -------- OpenTelemetry SDK --------
        sdk = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setMeterProvider(meterProvider)
            .setLoggerProvider(loggerProvider)
            .setPropagators(ContextPropagators.create(globalPropagator))
            .build();

        // -------- Micrometer Registry --------
        // Tenta criar PrometheusMeterRegistry via reflection; se não existir, usa SimpleMeterRegistry.
        this.micrometerRegistry = cfg.enableMicrometerBridge
            ? tryCreatePrometheusRegistryOrSimple()
            : new SimpleMeterRegistry();
    }

    // ===== Helpers: OTLP exporters =====

    private SpanExporter buildOtlpSpanExporter(TelemetryConfig cfg) {
        if (cfg.otlpProtocol == TelemetryConfig.Protocol.GRPC) {
            var b = OtlpGrpcSpanExporter.builder()
                .setEndpoint(cfg.otlpEndpoint.toString());
            cfg.headers.forEach(b::addHeader);
            return b.build();
        } else {
            var b = OtlpHttpSpanExporter.builder()
                .setEndpoint(cfg.otlpEndpoint.toString());
            cfg.headers.forEach(b::addHeader);
            return b.build();
        }
    }

    private MetricExporter buildOtlpMetricExporter(TelemetryConfig cfg) {
        if (cfg.otlpProtocol == TelemetryConfig.Protocol.GRPC) {
            var b = OtlpGrpcMetricExporter.builder()
                .setEndpoint(cfg.otlpEndpoint.toString());
            cfg.headers.forEach(b::addHeader);
            return b.build();
        } else {
            var b = OtlpHttpMetricExporter.builder()
                .setEndpoint(cfg.otlpEndpoint.toString());
            cfg.headers.forEach(b::addHeader);
            return b.build();
        }
    }

    private LogRecordExporter buildOtlpLogExporter(TelemetryConfig cfg) {
        if (cfg.otlpProtocol == TelemetryConfig.Protocol.GRPC) {
            var b = OtlpGrpcLogRecordExporter.builder()
                .setEndpoint(cfg.otlpEndpoint.toString());
            cfg.headers.forEach(b::addHeader);
            return b.build();
        } else {
            var b = OtlpHttpLogRecordExporter.builder()
                .setEndpoint(cfg.otlpEndpoint.toString());
            cfg.headers.forEach(b::addHeader);
            return b.build();
        }
    }

    private TextMapPropagator buildPropagators(TelemetryConfig cfg) {
        var list = new ArrayList<TextMapPropagator>();
        // W3C padrão
        list.add(io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator.getInstance());
        list.add(io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator.getInstance());
        // Extras opcionais
        for (String p : cfg.additionalPropagators) {
            if (p == null || p.isBlank()) continue;
            if (p.equalsIgnoreCase("b3")) list.add(B3Propagator.injectingSingleHeader());
            if (p.equalsIgnoreCase("b3multi")) list.add(B3Propagator.injectingMultiHeaders());
            if (p.equalsIgnoreCase("xray")) {
                try {
                    // Só funciona se a dependência estiver presente
                    list.add(AwsXrayPropagator.getInstance());
                } catch (NoClassDefFoundError __) {
                    // Ignora silenciosamente se não houver a dependência.
                }
            }
        }
        return TextMapPropagator.composite(list);
    }

    // ===== Opcional: Runtime Metrics via reflection =====
    private static void tryEnableRuntimeMetrics(SdkMeterProvider meterProvider, boolean enabled) {
        if (!enabled) return;
        try {
            // io.opentelemetry.contrib.runtime.metrics.RuntimeMetrics.enableAll().register(meterProvider);
            Class<?> rm = Class.forName("io.opentelemetry.contrib.runtime.metrics.RuntimeMetrics");
            var enableAll = rm.getMethod("enableAll");
            Object instance = enableAll.invoke(null);
            var register = instance.getClass().getMethod("register", SdkMeterProvider.class);
            register.invoke(instance, meterProvider);
        } catch (Throwable ignore) {
            // Sem a dependência no classpath: apenas não registra as métricas de runtime.
        }
    }

    // ===== Micrometer: tenta Prometheus, senão Simple =====
    private static MeterRegistry tryCreatePrometheusRegistryOrSimple() {
        try {
            Class<?> cfgClazz = Class.forName("io.micrometer.prometheus.PrometheusConfig");
            Object promCfg = cfgClazz.getField("DEFAULT").get(null);
            Class<?> regClazz = Class.forName("io.micrometer.prometheus.PrometheusMeterRegistry");
            Constructor<?> ctor = regClazz.getConstructor(cfgClazz);
            return (MeterRegistry) ctor.newInstance(promCfg);
        } catch (Throwable __) {
            return new SimpleMeterRegistry();
        }
    }

    // ===== TelemetryExporter API =====

    @Override
    public OpenTelemetry openTelemetry() { return sdk; }

    @Override
    public Tracer tracer(String instrumentationName) { return sdk.getTracer(instrumentationName); }

    @Override
    public Meter meter(String instrumentationName) { return sdk.getMeter(instrumentationName); }

    @Override
    public MeterRegistry meterRegistry() { return micrometerRegistry; }

    @Override
    public void forceFlush() {
        tracerProvider.forceFlush().join(10, TimeUnit.SECONDS);
        meterProvider.forceFlush().join(10, TimeUnit.SECONDS);
        loggerProvider.forceFlush().join(10, TimeUnit.SECONDS);
    }

    @Override
    public void close() {
        forceFlush();
        tracerProvider.shutdown().join(10, TimeUnit.SECONDS);
        meterProvider.shutdown().join(10, TimeUnit.SECONDS);
        loggerProvider.shutdown().join(10, TimeUnit.SECONDS);
    }

    // ===== No-op LogRecordExporter =====

    private static final class NoopLogRecordExporter implements LogRecordExporter {
        @Override
        public CompletableResultCode export(Collection<LogRecordData> logs) {
            return CompletableResultCode.ofSuccess();
        }
        @Override
        public CompletableResultCode flush() {
            return CompletableResultCode.ofSuccess();
        }
        @Override
        public CompletableResultCode shutdown() {
            return CompletableResultCode.ofSuccess();
        }
    }
}
