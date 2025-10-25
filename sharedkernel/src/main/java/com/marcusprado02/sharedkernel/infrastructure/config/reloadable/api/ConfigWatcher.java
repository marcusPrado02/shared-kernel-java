package com.marcusprado02.sharedkernel.infrastructure.config.reloadable.api;

import java.util.function.Consumer;

public interface ConfigWatcher extends AutoCloseable {
  void start(Consumer<ConfigSnapshot> onChange);
  void close();
}

