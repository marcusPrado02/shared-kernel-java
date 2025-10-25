package com.marcusprado02.sharedkernel.infrastructure.blob;

import java.net.URL;
import java.time.Instant;
import java.util.Map;

public record PresignResult(URL url, Instant expiresAt, Map<String,String> requiredHeaders){}
