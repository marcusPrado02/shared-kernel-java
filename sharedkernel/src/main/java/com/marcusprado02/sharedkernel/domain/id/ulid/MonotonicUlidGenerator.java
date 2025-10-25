package com.marcusprado02.sharedkernel.domain.id.ulid;


import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.marcusprado02.sharedkernel.domain.id.api.ClockSource;
import com.marcusprado02.sharedkernel.domain.id.api.EntropySource;
import com.marcusprado02.sharedkernel.domain.id.api.IdGenerator;

public final class MonotonicUlidGenerator implements IdGenerator<Ulid> {

    private static final int ENTROPY_BYTES = 10; // 80 bits
    private final ClockSource clock;
    private final EntropySource entropy;

    /** Estado global: último time-ms e os 80 bits (armazenados em dois longs). */
    private static final class State {
        final long timeMs;
        final long hi40; // 40 bits mais altos da entropia
        final long lo40; // 40 bits mais baixos
        State(long timeMs, long hi40, long lo40){ this.timeMs = timeMs; this.hi40 = hi40; this.lo40 = lo40; }
    }
    private final AtomicReference<State> last = new AtomicReference<>(new State(0, 0, 0));

    public MonotonicUlidGenerator(ClockSource clock, EntropySource entropy){
        this.clock = clock; this.entropy = entropy;
    }

    @Override
    public Ulid next() {
        while (true) {
            State prev = last.get();
            long now = clock.currentTimeMillis();
            if (now < prev.timeMs) now = prev.timeMs; // clamp para monotonicidade

            long hi40, lo40;
            if (now == prev.timeMs) {
                // incrementa contador 80-bit (hi40:lo40)
                hi40 = prev.hi40; lo40 = prev.lo40 + 1;
                if ((lo40 & ~0xFFFFFFFFFFL) != 0) { // overflow dos 40 bits
                    lo40 = 0;
                    hi40 = (hi40 + 1) & 0xFFFFFFFFFFL;
                    if (hi40 == 0) { // overflow total 80-bit no mesmo ms → espera próximo ms
                        do { now = clock.currentTimeMillis(); } while (now <= prev.timeMs);
                        // reinicializa entropia para o novo ms
                        long[] r = random80();
                        hi40 = r[0]; lo40 = r[1];
                    }
                }
                State next = new State(now, hi40, lo40);
                if (last.compareAndSet(prev, next)) return buildUlid(next);
            } else {
                // novo milissegundo: semente aleatória para entropia
                long[] r = random80();
                State next = new State(now, r[0], r[1]);
                if (last.compareAndSet(prev, next)) return buildUlid(next);
            }
        }
    }

    private long[] random80(){
        byte[] buf = new byte[ENTROPY_BYTES];
        entropy.nextBytes(buf);
        long hi40 = ((long)(buf[0] & 0xFF) << 32) |
                    ((long)(buf[1] & 0xFF) << 24) |
                    ((long)(buf[2] & 0xFF) << 16) |
                    ((long)(buf[3] & 0xFF) << 8)  |
                    ((long)(buf[4] & 0xFF));
        long lo40 = ((long)(buf[5] & 0xFF) << 32) |
                    ((long)(buf[6] & 0xFF) << 24) |
                    ((long)(buf[7] & 0xFF) << 16) |
                    ((long)(buf[8] & 0xFF) << 8)  |
                    ((long)(buf[9] & 0xFF));
        return new long[]{hi40 & 0xFFFFFFFFFFL, lo40 & 0xFFFFFFFFFFL};
    }

    private Ulid buildUlid(State s){
        byte[] out = new byte[16];
        // 48-bit time
        out[0] = (byte)(s.timeMs >>> 40);
        out[1] = (byte)(s.timeMs >>> 32);
        out[2] = (byte)(s.timeMs >>> 24);
        out[3] = (byte)(s.timeMs >>> 16);
        out[4] = (byte)(s.timeMs >>> 8);
        out[5] = (byte)(s.timeMs);
        // 80-bit entropy: hi40 + lo40
        out[6]  = (byte)(s.hi40 >>> 32);
        out[7]  = (byte)(s.hi40 >>> 24);
        out[8]  = (byte)(s.hi40 >>> 16);
        out[9]  = (byte)(s.hi40 >>> 8);
        out[10] = (byte)(s.hi40);
        out[11] = (byte)(s.lo40 >>> 32);
        out[12] = (byte)(s.lo40 >>> 24);
        out[13] = (byte)(s.lo40 >>> 16);
        out[14] = (byte)(s.lo40 >>> 8);
        out[15] = (byte)(s.lo40);
        return Ulid.ofBytes(out);
    }
}
