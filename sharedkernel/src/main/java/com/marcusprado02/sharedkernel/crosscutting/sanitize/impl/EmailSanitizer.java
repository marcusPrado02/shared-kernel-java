package com.marcusprado02.sharedkernel.crosscutting.sanitize.impl;


import java.net.URI;
import java.util.Map;

import com.marcusprado02.sharedkernel.crosscutting.sanitize.core.*;
import com.marcusprado02.sharedkernel.crosscutting.sanitize.rules.StringRules;
import com.marcusprado02.sharedkernel.crosscutting.sanitize.rules.StringValidations;

public final class EmailSanitizer implements Sanitizer<String>, SanitizerProvider {
    private final Sanitizer<String> pipeline;
    public EmailSanitizer(){
        this.pipeline = new PipelineSanitizer.Builder<String>()
            .rule(StringRules.stripZeroWidth())
            .rule(StringRules.stripBidi())
            .rule(StringRules.trim())
            .rule(StringRules.lowerCase())
            .validate(StringValidations.regex("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$"))
            .build();
    }
    @Override public String sanitize(String input, SanitizationContext ctx){ return pipeline.sanitize(input, ctx); }
    @Override public boolean supports(URI uri){ return "san".equals(uri.getScheme()) && "email".equals(uri.getHost()); }
    @Override public Sanitizer<?> create(URI uri, Map<String,?> d){ return this; }
}
