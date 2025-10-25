package com.marcusprado02.sharedkernel.observability.tracing.samplers.util;

import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

public final class DeterministicHash {
    private DeterministicHash(){}
    public static double hashToUnit(String s){
        if (s == null || s.isBlank()) return 0.0;
        CRC32 crc = new CRC32();
        crc.update(s.getBytes(StandardCharsets.UTF_8));
        long v = crc.getValue() & 0xffffffffL;
        return (v / (double)0x1_0000_0000L); // [0,1)
    }
}
