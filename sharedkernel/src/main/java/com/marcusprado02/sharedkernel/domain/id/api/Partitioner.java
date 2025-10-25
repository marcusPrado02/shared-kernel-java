package com.marcusprado02.sharedkernel.domain.id.api;

public interface Partitioner {
    /** 0..(2^bits-1). Deve caber em 'reservedEntropyBits'. */
    int partitionId();
    int bits(); // ex.: 12 bits => 4096 shards
}
