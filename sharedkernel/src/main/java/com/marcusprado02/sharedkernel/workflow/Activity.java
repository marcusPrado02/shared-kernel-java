package com.marcusprado02.sharedkernel.workflow;

public interface Activity<I, O> {
    O execute(ActivityContext ctx, I input) throws Exception;
    default String idempotencyKey(ActivityContext ctx, I input) { return null; } // p/ dedupe
}
