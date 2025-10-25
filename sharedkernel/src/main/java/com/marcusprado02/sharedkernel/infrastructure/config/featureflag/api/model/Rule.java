package com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.model;

import java.util.List;

public record Rule(String id, List<Condition> when, Rollout rollout) {}
