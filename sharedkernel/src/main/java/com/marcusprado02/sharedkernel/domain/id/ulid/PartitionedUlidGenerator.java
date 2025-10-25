package com.marcusprado02.sharedkernel.domain.id.ulid;

import com.marcusprado02.sharedkernel.domain.id.api.IdGenerator;
import com.marcusprado02.sharedkernel.domain.id.api.Partitioner;

public final class PartitionedUlidGenerator implements IdGenerator<Ulid> {
    private final MonotonicUlidGenerator delegate;
    private final Partitioner partitioner;

    public PartitionedUlidGenerator(MonotonicUlidGenerator delegate, Partitioner partitioner){
        if (partitioner.bits() <= 0 || partitioner.bits() > 20) // limite prático
            throw new IllegalArgumentException("partition bits must be 1..20");
        this.delegate = delegate; this.partitioner = partitioner;
    }

    @Override public Ulid next() {
        Ulid base = delegate.next();
        byte[] b = base.toBytes();
        // Bits mais altos da entropia ficam em b[6]..b[10].
        int bits = partitioner.bits();
        int pid  = partitioner.partitionId() & ((1 << bits) - 1);
        // injeta nos 'bits' mais à esquerda da entropia (b[6] tem os 8 bits mais altos).
        int shift = 8 - bits;
        int mask  = ((1 << bits) - 1) << shift;
        b[6] = (byte)((b[6] & ~mask) | (pid << shift));
        return Ulid.ofBytes(b);
    }
}
