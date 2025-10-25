package com.marcusprado02.sharedkernel.domain.id.example.impl;

import java.util.concurrent.ThreadLocalRandom;

import com.marcusprado02.sharedkernel.domain.id.api.EntropySource;

public final class FastEntropy implements EntropySource {
    @Override public void nextBytes(byte[] buffer) { ThreadLocalRandom.current().nextBytes(buffer); }
    @Override public String name(){ return "fast"; }
}
