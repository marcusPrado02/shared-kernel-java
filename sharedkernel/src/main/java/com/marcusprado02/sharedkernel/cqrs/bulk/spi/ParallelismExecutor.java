package com.marcusprado02.sharedkernel.cqrs.bulk.spi;

public interface ParallelismExecutor {
    void submit(Runnable r);
    static ParallelismExecutor virtualThreads(){
        return r -> Thread.startVirtualThread(r);
    }
}
