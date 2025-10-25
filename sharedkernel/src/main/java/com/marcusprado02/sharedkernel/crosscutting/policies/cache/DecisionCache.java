package com.marcusprado02.sharedkernel.crosscutting.policies.cache;

import java.util.Optional;

import com.marcusprado02.sharedkernel.crosscutting.policies.core.*;

public interface DecisionCache {
    Optional<Decision> get(String key);
    void put(String key, Decision decision, long ttlSec);
    String key(String tenant, Subject s, String action, Resource r, Environment e);
}
