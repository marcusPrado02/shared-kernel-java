package com.marcusprado02.sharedkernel.domain.id.example.impl;


import java.security.SecureRandom;

import com.marcusprado02.sharedkernel.domain.id.api.EntropySource;

public final class SecureEntropy implements EntropySource {
    private final SecureRandom rng = new SecureRandom();
    @Override public void nextBytes(byte[] buffer){ rng.nextBytes(buffer); }
    @Override public String name(){ return "secure"; }
}
