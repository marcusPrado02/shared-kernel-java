package com.marcusprado02.sharedkernel.infrastructure.config.reloadable.api;

public interface ConfigParser<T> {
  T parse(ConfigSnapshot snapshot) throws Exception;
}