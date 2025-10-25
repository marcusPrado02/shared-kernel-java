package com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.model;

public record Variant(String name, Object value, String type /*BOOL,STRING,NUMBER,JSON*/){}
