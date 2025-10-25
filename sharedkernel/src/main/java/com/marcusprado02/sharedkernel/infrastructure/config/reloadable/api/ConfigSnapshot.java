package com.marcusprado02.sharedkernel.infrastructure.config.reloadable.api;

public record ConfigSnapshot(
  String version,      // e.g. git sha, etag, resourceVersion
  byte[] content,      // YAML/JSON/properties
  String mediaType     // "application/x-yaml", "application/json", ...
) {}

