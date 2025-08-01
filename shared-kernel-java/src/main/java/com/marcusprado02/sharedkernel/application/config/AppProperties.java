package com.marcusprado02.sharedkernel.application.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    @NotBlank
    private String name;

    private String environment; // dev, staging, prod...

    private boolean debug;

    private FeatureFlags features;

    private RateLimitConfig rateLimit;

    private SecretsConfig secrets;

    private EnvironmentConfig env;

}
