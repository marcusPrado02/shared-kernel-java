package com.marcusprado02.sharedkernel.cqrs.handler.concurrency;

public interface OptimisticLockFacade {
    <A> A withOptimisticLock(String aggregateId, java.util.concurrent.Callable<A> action) throws Exception;
}
