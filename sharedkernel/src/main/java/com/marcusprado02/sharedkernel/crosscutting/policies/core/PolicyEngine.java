package com.marcusprado02.sharedkernel.crosscutting.policies.core;

public interface PolicyEngine {
    Decision decide(Subject subject, String action, Resource resource, Environment env);
}

