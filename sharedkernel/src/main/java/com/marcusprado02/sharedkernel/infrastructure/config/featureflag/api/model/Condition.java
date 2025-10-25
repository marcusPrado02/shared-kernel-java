package com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.model;

public record Condition(String attribute, Operator op, Object value) {}