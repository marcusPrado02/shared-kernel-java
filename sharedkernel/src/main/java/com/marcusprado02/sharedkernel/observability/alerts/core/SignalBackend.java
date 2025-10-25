package com.marcusprado02.sharedkernel.observability.alerts.core;

/** Onde a consulta irá rodar. */
public enum SignalBackend { PROMETHEUS, LOKI, ELASTIC, CLOUDWATCH, DATADOG }