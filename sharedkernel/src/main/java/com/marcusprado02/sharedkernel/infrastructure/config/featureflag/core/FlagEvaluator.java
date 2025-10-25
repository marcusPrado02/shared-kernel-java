package com.marcusprado02.sharedkernel.infrastructure.config.featureflag.core;

import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.EvaluationDetail;
import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.FlagContext;
import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.model.FlagDefinition;

public interface FlagEvaluator {
  <T> EvaluationDetail<T> evaluate(FlagDefinition def, Class<T> type, T defaultValue, FlagContext ctx);
}
