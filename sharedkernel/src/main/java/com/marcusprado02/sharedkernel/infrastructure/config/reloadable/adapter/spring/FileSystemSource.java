package com.marcusprado02.sharedkernel.infrastructure.config.reloadable.adapter.spring;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import com.marcusprado02.sharedkernel.infrastructure.config.reloadable.api.ConfigSnapshot;
import com.marcusprado02.sharedkernel.infrastructure.config.reloadable.api.ConfigSource;

public final class FileSystemSource implements ConfigSource {
  private final Path path;

  public FileSystemSource(Path path) { this.path = path; }

  @Override public Optional<ConfigSnapshot> load() {
    try {
      byte[] bytes = Files.readAllBytes(path);
      String ver = Files.getLastModifiedTime(path).toMillis() + "";
      return Optional.of(new ConfigSnapshot(ver, bytes, "application/x-yaml"));
    } catch (IOException e) {
      return Optional.empty();
    }
  }

  @Override public String id() { return "file:" + path.toAbsolutePath(); }
  public Path getPath() { return path; }
}