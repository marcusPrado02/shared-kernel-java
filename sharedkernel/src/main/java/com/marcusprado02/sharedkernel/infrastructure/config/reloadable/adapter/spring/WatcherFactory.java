package com.marcusprado02.sharedkernel.infrastructure.config.reloadable.adapter.spring;


import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.marcusprado02.sharedkernel.infrastructure.config.reloadable.api.ConfigSnapshot;
import com.marcusprado02.sharedkernel.infrastructure.config.reloadable.api.ConfigSource;
import com.marcusprado02.sharedkernel.infrastructure.config.reloadable.api.ConfigWatcher;

public final class WatcherFactory {

  private WatcherFactory(){}

  public static ConfigWatcher create(ConfigSource source, FlagsProperties.Watch watch) {
    if (!watch.isEnabled()) return NoopWatcher.INSTANCE;

    if (source instanceof FileSystemSource fs) {
      return new FileWatcher(fs.getPath());
    }
    if (source instanceof HttpSource http) {
      return new HttpPollingWatcher(http, watch.getInterval());
    }
    // K8s/Consul: em prod, usar watch/long-poll do provider. Aqui no-op.
    return NoopWatcher.INSTANCE;
  }

  // --------- Implementações ---------

  static final class NoopWatcher implements ConfigWatcher {
    static final NoopWatcher INSTANCE = new NoopWatcher();
    @Override public void start(Consumer<ConfigSnapshot> onChange) {}
    @Override public void close() {}
  }

  static final class FileWatcher implements ConfigWatcher, AutoCloseable {
    private final Path path;
    private volatile boolean running;
    private Thread thread;

    FileWatcher(Path path) { this.path = path; }

    @Override public void start(Consumer<ConfigSnapshot> onChange) {
      running = true;
      thread = new Thread(() -> {
        try (WatchService ws = FileSystems.getDefault().newWatchService()) {
          path.getParent().register(ws, StandardWatchEventKinds.ENTRY_MODIFY);
          while (running) {
            WatchKey key = ws.take();
            key.pollEvents().forEach(ev -> {
              var changed = (java.nio.file.Path) ev.context();
              if (changed != null && changed.getFileName().toString().equals(path.getFileName().toString())) {
                // Recarrega do FileSystemSource
                Optional<ConfigSnapshot> snap = new FileSystemSource(path).load();
                snap.ifPresent(onChange);
              }
            });
            key.reset();
          }
        } catch (InterruptedException | IOException ignored) {}
      }, "flags-file-watcher");
      thread.setDaemon(true);
      thread.start();
    }

    @Override public void close() { running = false; if (thread != null) thread.interrupt(); }
  }

  static final class HttpPollingWatcher implements ConfigWatcher, AutoCloseable {
    private final HttpSource http;
    private final Duration interval;
    private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread t = new Thread(r, "flags-http-poller"); t.setDaemon(true); return t;
    });

    HttpPollingWatcher(HttpSource http, Duration interval) {
      this.http = http; this.interval = interval == null ? Duration.ofSeconds(2) : interval;
    }

    @Override public void start(Consumer<ConfigSnapshot> onChange) {
      exec.scheduleAtFixedRate(() -> {
        try {
          var snap = http.load();
          snap.ifPresent(onChange);
        } catch (Exception ignored) {}
      }, 0, Math.max(1, interval.toMillis()), TimeUnit.MILLISECONDS);
    }

    @Override public void close() { exec.shutdownNow(); }
  }
}
