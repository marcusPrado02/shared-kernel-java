package com.marcusprado02.sharedkernel.crosscutting.transformers.core;

import java.util.Map;

public record SideOutput<T>(String channel, T payload, Map<String, Object> meta) {}
