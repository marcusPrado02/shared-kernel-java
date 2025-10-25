package com.marcusprado02.sharedkernel.domain.id.api;

public interface EntropySource {
    /** Fill the buffer with random bytes. */
    void nextBytes(byte[] buffer);
    String name();
}
