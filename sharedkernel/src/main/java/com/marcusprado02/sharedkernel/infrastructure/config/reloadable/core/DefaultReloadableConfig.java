package com.marcusprado02.sharedkernel.infrastructure.config.reloadable.core;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.marcusprado02.sharedkernel.infrastructure.config.reloadable.api.ConfigParser;
import com.marcusprado02.sharedkernel.infrastructure.config.reloadable.api.ConfigSnapshot;
import com.marcusprado02.sharedkernel.infrastructure.config.reloadable.api.ConfigSource;
import com.marcusprado02.sharedkernel.infrastructure.config.reloadable.api.ReloadableConfig;
import com.marcusprado02.sharedkernel.infrastructure.config.validation.api.ConfigValidator;

import io.micrometer.core.instrument.MeterRegistry;

public final class DefaultReloadableConfig<T> implements ReloadableConfig<T> {
  private final AtomicReference<T> ref = new AtomicReference<>();
  private final AtomicReference<String> ver = new AtomicReference<>("unknown");
  private final ConfigSource source;
  private final ConfigParser<T> parser;
  private final ConfigValidator<T> validator;
  private final List<Consumer<T>> listeners = new CopyOnWriteArrayList<>();
  private final MeterRegistry meters;

  public DefaultReloadableConfig(ConfigSource source, ConfigParser<T> parser,
                                 ConfigValidator<T> validator, MeterRegistry meters) throws Exception {
    this.source = source; this.parser = parser; this.validator = validator; this.meters = meters;
    // bootstrap
    var snap = source.load().orElseThrow(() -> new IllegalStateException("Config source empty: " + source.id()));
    apply(snap, true);
  }

  public void onSnapshot(ConfigSnapshot snap) throws Exception { apply(snap, false); }

  private void apply(ConfigSnapshot snap, boolean boot) throws Exception {
    var start = System.nanoTime();
    var prev = ref.get(); var prevVer = ver.get();
    try {
      var parsed = parser.parse(snap);
      var result = validator.validate(parsed);
      if (!result.valid()) {
        meters.counter("config.reload.validation_failed", "source", source.id()).increment();
        throw new IllegalArgumentException("Invalid config: " + result.message());
      }
      ref.set(parsed); ver.set(snap.version());
      listeners.forEach(l -> l.accept(parsed));
      meters.counter("config.reload.applied", "source", source.id()).increment();
    } catch (Exception e) {
      // rollback
      ref.set(prev); ver.set(prevVer);
      meters.counter("config.reload.rollback", "source", source.id()).increment();
      if (boot) throw e; // fail-fast on bootstrap
    } finally {
      meters.timer("config.reload.latency", "source", source.id()).record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
    }
  }

  @Override public T get() { return ref.get(); }
  @Override public void addListener(Consumer<T> l) { listeners.add(l); }
  @Override public String currentVersion() { return ver.get(); }
}
