package com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api;

import java.util.Optional;

public interface FeatureFlagClient {
  boolean bool(String key, boolean defaultValue, FlagContext ctx);
  long    number(String key, long defaultValue, FlagContext ctx);
  double  decimal(String key, double defaultValue, FlagContext ctx);
  String  string(String key, String defaultValue, FlagContext ctx);
  <T> EvaluationDetail<T> eval(String key, Class<T> type, T defaultValue, FlagContext ctx);
}