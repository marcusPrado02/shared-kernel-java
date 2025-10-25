package com.marcusprado02.sharedkernel.domain.snapshot.service;

import java.util.zip.Deflater;
import java.util.zip.Inflater;

import com.marcusprado02.sharedkernel.domain.snapshot.ports.SnapshotCompressor;

public final class DefaultSnapshotCompressor implements SnapshotCompressor {
    @Override public byte[] compress(byte[] input) {
        var deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        deflater.setInput(input); deflater.finish();
        byte[] buf = new byte[input.length + 128];
        int len = deflater.deflate(buf);
        byte[] out = new byte[len];
        System.arraycopy(buf, 0, out, 0, len);
        return out;
    }
    @Override public byte[] decompress(byte[] input) {
        try {
            var inflater = new Inflater(true);
            inflater.setInput(input);
            byte[] buf = new byte[input.length * 8];
            int len = inflater.inflate(buf);
            byte[] out = new byte[len];
            System.arraycopy(buf, 0, out, 0, len);
            return out;
        } catch (Exception e) { throw new RuntimeException(e); }
    }
    @Override public String name() { return "deflate"; }
}

