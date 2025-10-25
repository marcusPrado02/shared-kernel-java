package com.marcusprado02.sharedkernel.crosscutting.sanitize.impl;


import java.net.URI;
import java.util.Map;

import com.marcusprado02.sharedkernel.crosscutting.sanitize.core.*;
import com.marcusprado02.sharedkernel.crosscutting.sanitize.rules.StringRules;

public final class PlainStringSanitizer implements Sanitizer<String>, SanitizerProvider {
    private final Sanitizer<String> pipeline;

    public PlainStringSanitizer(int maxLen, boolean nfkc){
        var b = new PipelineSanitizer.Builder<String>()
            .rule(StringRules.stripZeroWidth())
            .rule(StringRules.stripBidi())
            .rule(StringRules.stripControl())
            .rule(nfkc? StringRules.toNFKC() : StringRules.toNFC())
            .rule(StringRules.collapseWhitespace());
        if (maxLen>0) b.rule(StringRules.maxLength(maxLen));
        this.pipeline = b.build();
    }

    @Override public String sanitize(String input, SanitizationContext ctx) { return pipeline.sanitize(input, ctx); }

    // Provider
    @Override public boolean supports(URI uri){ return "san".equals(uri.getScheme()) && "plain".equals(uri.getHost()); }
    @Override public Sanitizer<?> create(URI uri, Map<String,?> defaults) {
        var q = query(uri);
        int max = Integer.parseInt(q.getOrDefault("max", "0"));
        boolean nfkc = Boolean.parseBoolean(q.getOrDefault("nfkc", "true"));
        return new PlainStringSanitizer(max, nfkc);
    }

    private static Map<String,String> query(URI u){
        if (u.getQuery()==null) return Map.of();
        return java.util.Arrays.stream(u.getQuery().split("&"))
            .map(s->s.split("="))
            .collect(java.util.stream.Collectors.toMap(a->a[0], a->a.length>1?a[1]:""));
    }
}