package com.marcusprado02.sharedkernel.domain.id.ulid;


final class UlidCodec {
    private static final char[] CROCKFORD = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();
    private static final int[] DECODE = new int[128];
    static {
        java.util.Arrays.fill(DECODE, -1);
        String map = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";
        for (int i=0; i<map.length(); i++) {
            char c = map.charAt(i);
            DECODE[c] = i;
            DECODE[Character.toLowerCase(c)] = i;
        }
        // aliases comuns
        DECODE['I'] = DECODE['i'] = 1; // I -> 1
        DECODE['L'] = DECODE['l'] = 1; // L -> 1
        DECODE['O'] = DECODE['o'] = 0; // O -> 0
        // U é excluído por ambiguidade; não mapear
    }

    private UlidCodec(){}

    /** 16 bytes -> 26 chars */
    static String encode(byte[] data){
        if (data.length != 16) throw new IllegalArgumentException("needs 16 bytes");
        // Concatena 128 bits e emite 26 grupos de 5 bits (últimos 2 bits com padding lógico)
        long hi = bytesToLong(data, 0);
        long lo = bytesToLong(data, 8);
        char[] out = new char[26];
        // 128 bits -> 26*5=130 bits; os 2 bits extras ficam zero (não usados).
        long[] parts = new long[]{ // extrai da esquerda p/ direita
            (hi >>> 59) & 0x1F, (hi >>> 54) & 0x1F, (hi >>> 49) & 0x1F, (hi >>> 44) & 0x1F, (hi >>> 39) & 0x1F,
            (hi >>> 34) & 0x1F, (hi >>> 29) & 0x1F, (hi >>> 24) & 0x1F, (hi >>> 19) & 0x1F, (hi >>> 14) & 0x1F,
            (hi >>> 9)  & 0x1F, (hi >>> 4)  & 0x1F,
            ((hi & 0xF) << 1) | ((lo >>> 63) & 0x1),
            (lo >>> 58) & 0x1F, (lo >>> 53) & 0x1F, (lo >>> 48) & 0x1F, (lo >>> 43) & 0x1F,
            (lo >>> 38) & 0x1F, (lo >>> 33) & 0x1F, (lo >>> 28) & 0x1F, (lo >>> 23) & 0x1F,
            (lo >>> 18) & 0x1F, (lo >>> 13) & 0x1F, (lo >>> 8)  & 0x1F, (lo >>> 3)  & 0x1F,
            (lo & 0x7) << 2 // padding (2 bits zeros no final)
        };
        for (int i=0;i<26;i++) out[i] = CROCKFORD[(int)parts[i]];
        return new String(out);
    }

    /** 26 chars -> 16 bytes */
    static byte[] decode(String s){
        if (s.length() != 26) throw new IllegalArgumentException("ULID string must be 26 chars");
        int[] v = new int[26];
        for (int i=0;i<26;i++){
            char c = s.charAt(i);
            if (c >= 128 || DECODE[c] < 0) throw new IllegalArgumentException("invalid ULID char: "+c);
            v[i] = DECODE[c];
        }
        long hi =
            ((long)v[0]  << 59) | ((long)v[1]  << 54) | ((long)v[2]  << 49) | ((long)v[3]  << 44) |
            ((long)v[4]  << 39) | ((long)v[5]  << 34) | ((long)v[6]  << 29) | ((long)v[7]  << 24) |
            ((long)v[8]  << 19) | ((long)v[9]  << 14) | ((long)v[10] << 9)  | ((long)v[11] << 4) |
            ((long)v[12] >>> 1);
        long lo =
            ((long)(v[12] & 0x1) << 63) | ((long)v[13] << 58) | ((long)v[14] << 53) | ((long)v[15] << 48) |
            ((long)v[16] << 43) | ((long)v[17] << 38) | ((long)v[18] << 33) | ((long)v[19] << 28) |
            ((long)v[20] << 23) | ((long)v[21] << 18) | ((long)v[22] << 13) | ((long)v[23] << 8)  |
            ((long)v[24] << 3)  | ((long)v[25] >>> 2);
        byte[] out = new byte[16];
        longToBytes(hi, out, 0);
        longToBytes(lo, out, 8);
        return out;
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
