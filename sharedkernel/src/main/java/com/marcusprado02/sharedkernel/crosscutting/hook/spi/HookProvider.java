package com.marcusprado02.sharedkernel.crosscutting.hook.spi;

import java.net.URI;
import java.util.Map;

import com.marcusprado02.sharedkernel.crosscutting.hook.*;

public interface HookProvider {
    boolean supports(URI uri);
    HookRegistration<?> create(URI uri, Map<String,?> defaults, Telemetry telemetry);
}
