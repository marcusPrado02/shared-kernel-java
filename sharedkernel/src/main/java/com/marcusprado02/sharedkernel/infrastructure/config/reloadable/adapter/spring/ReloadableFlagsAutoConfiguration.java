package com.marcusprado02.sharedkernel.infrastructure.config.reloadable.adapter.spring;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.model.FlagDefinition;
import com.marcusprado02.sharedkernel.infrastructure.config.reloadable.api.ConfigSource;
import com.marcusprado02.sharedkernel.infrastructure.config.reloadable.api.ConfigWatcher;
import com.marcusprado02.sharedkernel.infrastructure.config.reloadable.core.DefaultReloadableConfig;
import com.marcusprado02.sharedkernel.infrastructure.config.validation.api.ConfigValidator;

import io.micrometer.core.instrument.MeterRegistry;

@Configuration
@EnableConfigurationProperties(FlagsProperties.class)
public class ReloadableFlagsAutoConfiguration {

  @Bean
  FlagsReloadable flagsReloadable(FlagsProperties props, MeterRegistry meters, ConfigValidator<Map<String, FlagDefinition>> validator) throws Exception {
    ConfigSource source = switch (props.getSource().getType()) {
      case FILE -> new FileSystemSource(Paths.get(props.getSource().getPath()));
      case HTTP -> new HttpSource(props.getSource().getUrl(), props.getSource().getAuth());
      case K8S  -> new K8sConfigMapSource(props.getK8s());
      case CONSUL -> new ConsulSource(props.getConsul());
    };
    var parser = new FlagsConfigParser();
    var reloadable = new DefaultReloadableConfig<>(source, parser, validator, meters);
    // wire watcher
    ConfigWatcher watcher = WatcherFactory.create(source, props.getWatch());
    watcher.start(t -> {
      try {
        reloadable.onSnapshot(t);
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    });
    return new FlagsReloadable() {
      @Override public Map<String, FlagDefinition> get() { return reloadable.get(); }
      @Override public void addListener(Consumer<Map<String, FlagDefinition>> l){ reloadable.addListener(l); }
      @Override public String currentVersion(){ return reloadable.currentVersion(); }
    };
  }

  @Bean
  @ConditionalOnMissingBean
  ConfigValidator<Map<String, FlagDefinition>> flagsValidator() {
    return ConfigValidator.composite(List.of(new FlagsJsonSchemaValidator("/schemas/flags.schema.json"),
                                             new FlagsSemanticValidator()));
  }
}
