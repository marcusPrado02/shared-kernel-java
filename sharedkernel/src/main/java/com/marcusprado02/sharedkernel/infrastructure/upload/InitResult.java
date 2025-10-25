package com.marcusprado02.sharedkernel.infrastructure.upload;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

public record InitResult(String uploadId, URI location, Map<String,Object> extra) {}
