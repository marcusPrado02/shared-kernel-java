package com.marcusprado02.sharedkernel.observability.tracing.samplers.util;


import java.util.concurrent.atomic.LongAdder;

public final class SlidingWindowCounter {
    private final int buckets; private final long bucketMillis;
    private final LongAdder[] arr; private volatile long start; private volatile int head;

    public SlidingWindowCounter(int buckets, long bucketMillis) {
        this.buckets = Math.max(1, buckets);
        this.bucketMillis = Math.max(50, bucketMillis);
        this.arr = new LongAdder[this.buckets];
        for (int i=0;i<buckets;i++) arr[i] = new LongAdder();
        this.start = System.currentTimeMillis();
        this.head = 0;
    }
    public void inc(){
        roll();
        arr[head].increment();
    }
    public long sum(){
        roll();
        long s = 0; for (var a : arr) s += a.sum(); return s;
    }
    private void roll(){
        long now = System.currentTimeMillis();
        long diff = now - start;
        int steps = (int)(diff / bucketMillis);
        if (steps <= 0) return;
        synchronized (this){
            diff = System.currentTimeMillis() - start;
            steps = (int)(diff / bucketMillis);
            if (steps <= 0) return;
            for (int i=0;i<steps && i<buckets;i++){
                head = (head + 1) % buckets;
                arr[head] = new LongAdder(); // zera bucket que “saiu”
            }
            start += steps * bucketMillis;
        }
    }
}
