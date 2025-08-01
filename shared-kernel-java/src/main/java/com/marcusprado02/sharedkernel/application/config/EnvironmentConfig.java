package com.marcusprado02.sharedkernel.application.config;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnvironmentConfig {

    private String region;

    private String zone;

    private String appVersion;

    private String buildHash;
}
