package com.marcusprado02.sharedkernel.domain.id.ulid;


import java.util.Arrays;

/** Imutável, 128 bits ULID: 48-bit time (ms) + 80-bit entropy. */
public final class Ulid implements Comparable<Ulid> {
    private final byte[] bytes; // 16 bytes

    private Ulid(byte[] bytes) {
        if (bytes.length != 16) throw new IllegalArgumentException("ULID must be 16 bytes");
        this.bytes = bytes.clone();
    }

    public static Ulid ofBytes(byte[] bytes){ return new Ulid(bytes); }

    /** time-ms é armazenado nos 6 primeiros bytes big-endian. */
    public long time() {
        return ((long)(bytes[0] & 0xFF) << 40)
             | ((long)(bytes[1] & 0xFF) << 32)
             | ((long)(bytes[2] & 0xFF) << 24)
             | ((long)(bytes[3] & 0xFF) << 16)
             | ((long)(bytes[4] & 0xFF) << 8)
             | ((long)(bytes[5] & 0xFF));
    }

    public byte[] toBytes(){ return bytes.clone(); }

    @Override public int compareTo(Ulid o) {
        for (int i=0;i<16;i++){
            int a = bytes[i] & 0xFF, b = o.bytes[i] & 0xFF;
            if (a != b) return Integer.compare(a, b);
        }
        return 0;
    }

    @Override public boolean equals(Object o){ return (o instanceof Ulid u) && Arrays.equals(bytes, u.bytes); }
    @Override public int hashCode(){ return Arrays.hashCode(bytes); }

    /** 26 chars Base32 Crockford; lex order == time order. */
    @Override public String toString(){ return UlidCodec.encode(bytes); }

    public static Ulid parse(String s){ return new Ulid(UlidCodec.decode(s)); }
}

