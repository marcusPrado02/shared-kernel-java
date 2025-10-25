package com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.model;

public record Bucket(String variant, int weight /*0..10000*/, String seedAttribute /*userId, sessionId, ...*/) {}
