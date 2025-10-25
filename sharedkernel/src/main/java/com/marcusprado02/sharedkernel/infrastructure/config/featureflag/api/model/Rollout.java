package com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.model;

import java.util.List;

public record Rollout(List<Bucket> buckets) {}

