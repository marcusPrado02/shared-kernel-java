package com.marcusprado02.sharedkernel.infrastructure.config.featureflag.adapter.spring;

import java.util.Map;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.FeatureFlagClient;
import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.model.FlagDefinition;
import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.core.CachedFlagStore;
import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.core.DefaultFeatureFlagClient;
import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.core.DeterministicFlagEvaluator;
import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.core.FlagEvaluator;
import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.core.FlagProvider;
import com.marcusprado02.sharedkernel.infrastructure.config.reloadable.adapter.spring.FlagsReloadable;

import io.micrometer.core.instrument.MeterRegistry;

@Configuration
@ConditionalOnClass(MeterRegistry.class)
public class FeatureFlagAutoConfiguration {

  @Bean @ConditionalOnMissingBean
  FlagEvaluator flagEvaluator(){ return new DeterministicFlagEvaluator(); }

  @Bean @ConditionalOnMissingBean
  CachedFlagStore flagStore(){ return new CachedFlagStore(); }

  @Bean
  @ConditionalOnMissingBean(FlagProvider.class)
  FlagProvider flagProvider(FlagsReloadable reloadable) {
    return new FlagProvider() {
      @Override public Optional<FlagDefinition> get(String key){ return Optional.ofNullable(reloadable.get().get(key)); }
      @Override public Map<String, FlagDefinition> getAll(){ return reloadable.get(); }
      @Override public String providerName(){ return "reloadable"; }
    };
  }

  @Bean
  FeatureFlagClient featureFlagClient(FlagProvider provider, CachedFlagStore store, FlagEvaluator eval, MeterRegistry meters) {
    return new DefaultFeatureFlagClient(provider, store, eval, meters);
  }

  @Bean
  FlaggedAspect flaggedAspect(FeatureFlagClient c){ return new FlaggedAspect(c); }
}