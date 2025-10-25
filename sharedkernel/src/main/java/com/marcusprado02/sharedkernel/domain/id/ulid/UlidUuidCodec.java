package com.marcusprado02.sharedkernel.domain.id.ulid;


import java.util.UUID;

public final class UlidUuidCodec {
    public static UUID toUuid(Ulid u){
        byte[] b = u.toBytes();
        long msb = bytesToLong(b, 0);
        long lsb = bytesToLong(b, 8);
        return new UUID(msb, lsb);
    }
    public static Ulid fromUuid(UUID id){
        byte[] b = new byte[16];
        longToBytes(id.getMostSignificantBits(), b, 0);
        longToBytes(id.getLeastSignificantBits(), b, 8);
        return Ulid.ofBytes(b);
    }
    private static long bytesToLong(byte[] in, int off){
        return ((long)(in[off]   & 0xFF) << 56) |
               ((long)(in[off+1] & 0xFF) << 48) |
               ((long)(in[off+2] & 0xFF) << 40) |
               ((long)(in[off+3] & 0xFF) << 32) |
               ((long)(in[off+4] & 0xFF) << 24) |
               ((long)(in[off+5] & 0xFF) << 16) |
               ((long)(in[off+6] & 0xFF) << 8)  |
               ((long)(in[off+7] & 0xFF));
    }
    private static void longToBytes(long v, byte[] out, int off){
        out[off]   = (byte)(v >>> 56);
        out[off+1] = (byte)(v >>> 48);
        out[off+2] = (byte)(v >>> 40);
        out[off+3] = (byte)(v >>> 32);
        out[off+4] = (byte)(v >>> 24);
        out[off+5] = (byte)(v >>> 16);
        out[off+6] = (byte)(v >>> 8);
        out[off+7] = (byte)(v);
    }
}


