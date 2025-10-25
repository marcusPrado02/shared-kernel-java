package com.marcusprado02.sharedkernel.domain.service.example.policy;


public interface PromotionSpecification {
    boolean isEligible(Subject subject, Context context);

    record Subject(String customerId, String sku, int quantity, String category) {}
    record Context(String channel, String campaign, String tenant) {}
}
