package com.marcusprado02.sharedkernel.domain.snapshot.ports;

public interface SnapshotCompressor {
    byte[] compress(byte[] input);
    byte[] decompress(byte[] input);
    String name();
}
