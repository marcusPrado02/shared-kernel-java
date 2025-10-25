package com.marcusprado02.sharedkernel.infrastructure.telemetry;

import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;


final class NoopSpanExporter implements SpanExporter {
  @Override
  public CompletableResultCode export(java.util.Collection<SpanData> spans) {
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
