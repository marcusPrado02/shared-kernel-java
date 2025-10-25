package com.marcusprado02.sharedkernel.infrastructure.config.reloadable.api;

import java.util.function.Consumer;

public interface ReloadableConfig<T> {
  T get();                                 // referência atômica
  void addListener(Consumer<T> l);         // notifica após troca
  String currentVersion();
}