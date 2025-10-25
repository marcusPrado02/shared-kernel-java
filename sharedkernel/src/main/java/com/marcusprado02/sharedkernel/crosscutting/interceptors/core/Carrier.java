package com.marcusprado02.sharedkernel.crosscutting.interceptors.core;

import java.util.Map;
import java.util.Optional;

public interface Carrier {
    Optional<String> get(String key);
    void set(String key, String value);
    Map<String,String> dump();
}

