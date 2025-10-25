package com.marcusprado02.sharedkernel.crosscutting.helpers.env;

import java.util.Optional;

public final class EnvVarHelper {
  private EnvVarHelper(){}
  public static String required(String name) {
    String v = System.getenv(name);
    if (v == null || v.isBlank()) throw new IllegalStateException("Missing env: " + name);
    return v;
  }
  public static Optional<Integer> intVar(String name) {
    try { return Optional.of(Integer.parseInt(System.getenv(name))); }
    catch (Exception ignored) { return Optional.empty(); }
  }
}

