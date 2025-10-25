package com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.model;


public record Prerequisite(String flagKey, String requiredVariant) {}