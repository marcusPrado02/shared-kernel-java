package com.marcusprado02.sharedkernel.crosscutting.policies.core;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Slf4jDecisionLogger implements DecisionLogger {
    private static final Logger log = LoggerFactory.getLogger(Slf4jDecisionLogger.class);

    @Override
    public void logDecision(Decision d, Subject sub, String action, Resource res, Environment env) {
        log.info("POLICY decision={} policyId={} reason={} tenant={} action={} resourceType={} resourceId={}",
                d.effect(), d.policyId(), d.reason(), env.tenant(), action, res.type(), String.valueOf(res.id()));
    }
}