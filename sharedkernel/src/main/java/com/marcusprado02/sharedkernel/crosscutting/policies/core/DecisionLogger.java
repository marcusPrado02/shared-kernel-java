package com.marcusprado02.sharedkernel.crosscutting.policies.core;


public interface DecisionLogger {
    void logDecision(Decision d, Subject sub, String action, Resource res, Environment env);
}