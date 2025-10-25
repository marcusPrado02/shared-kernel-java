package com.marcusprado02.sharedkernel.infrastructure.config.featureflag.core;

import java.util.Map;
import java.util.Optional;

import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.EvaluationDetail;
import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.FeatureFlagClient;
import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.FlagContext;

import io.micrometer.core.instrument.MeterRegistry;

public final class DefaultFeatureFlagClient implements FeatureFlagClient {
  private final FlagProvider provider;
  private final CachedFlagStore store;
  private final FlagEvaluator evaluator;
  private final MeterRegistry meters;

  public DefaultFeatureFlagClient(FlagProvider provider, CachedFlagStore store, FlagEvaluator evaluator, MeterRegistry meters) {
    this.provider = provider; this.store = store; this.evaluator = evaluator; this.meters = meters;
    // pre-warm
    store.putAll(provider.getAll());
  }

  @Override public boolean bool(String key, boolean def, FlagContext ctx) {
    return eval(key, Boolean.class, def, ctx).value();
  }
  @Override public long number(String key, long def, FlagContext ctx) {
    return eval(key, Long.class, def, ctx).value();
  }
  @Override public double decimal(String key, double def, FlagContext ctx) {
    return eval(key, Double.class, def, ctx).value();
  }
  @Override public String string(String key, String def, FlagContext ctx) {
    return eval(key, String.class, def, ctx).value();
  }

  @Override public <T> EvaluationDetail<T> eval(String key, Class<T> type, T def, FlagContext ctx) {
    var start = System.nanoTime();
    try {
      var defOpt = store.get(key).or(() -> provider.get(key).map(d -> { store.put(d); return Optional.of(d); }).orElse(Optional.empty()));
      var detail = defOpt.<EvaluationDetail<T>>map(d -> evaluator.evaluate(d, type, def, ctx))
                         .orElse(new EvaluationDetail<>(def, key, "default", "NOT_FOUND", true, Map.of()));
      meters.counter("flags.eval.count", "key", key, "result", detail.isDefault() ? "default" : "match").increment();
      return detail;
    } finally {
      meters.timer("flags.eval.latency", "key", key).record(System.nanoTime() - start, java.util.concurrent.TimeUnit.NANOSECONDS);
    }
  }
}