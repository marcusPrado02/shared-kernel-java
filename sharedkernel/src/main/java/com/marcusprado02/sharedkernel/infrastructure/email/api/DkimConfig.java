package com.marcusprado02.sharedkernel.infrastructure.email.api;


public record DkimConfig(String domain, String selector, String privateKeyPem) {}

