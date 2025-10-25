package com.marcusprado02.sharedkernel.crosscutting.policies.core;

public interface PolicyEvaluator {
    Decision evaluate(Subject sub, String action, Resource res, Environment env) throws Exception;
}

