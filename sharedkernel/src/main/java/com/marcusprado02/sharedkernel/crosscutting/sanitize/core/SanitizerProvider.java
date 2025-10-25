package com.marcusprado02.sharedkernel.crosscutting.sanitize.core;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

public interface SanitizerProvider {
    boolean supports(URI uri);
    Sanitizer<?> create(URI uri, Map<String,?> defaults);
}
