package com.marcusprado02.sharedkernel.crosscutting.policies.core;


public final class PolicyDeniedException extends RuntimeException {
    private final Decision decision;

    public PolicyDeniedException(Decision decision) {
        super("Access denied: policyId=" + decision.policyId() + " reason=" + decision.reason());
        this.decision = decision;
    }

    public Decision decision() { return decision; }
}
