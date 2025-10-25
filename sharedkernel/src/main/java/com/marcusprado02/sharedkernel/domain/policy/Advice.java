package com.marcusprado02.sharedkernel.domain.policy;

import java.util.Map;

public record Advice(String id, Map<String,String> params) {}