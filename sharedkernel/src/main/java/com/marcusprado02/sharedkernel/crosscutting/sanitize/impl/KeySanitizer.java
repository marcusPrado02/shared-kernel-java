package com.marcusprado02.sharedkernel.crosscutting.sanitize.impl;


import java.net.URI;
import java.text.Normalizer;
import java.util.Map;

import com.marcusprado02.sharedkernel.crosscutting.sanitize.core.*;

public final class KeySanitizer implements Sanitizer<String>, SanitizerProvider {
    private final boolean lower;
    private final String allowed; // regex char-class, e.g., "a-z0-9:_-"
    private final int max;

    public KeySanitizer(boolean lower, String allowed, int max){
        this.lower=lower; this.allowed=allowed; this.max=max;
    }

    @Override public String sanitize(String in, SanitizationContext ctx) {
        if (in == null) return null;
        String s = Normalizer.normalize(in, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "");
        if (lower) s = s.toLowerCase(ctx.locale());
        s = s.replaceAll("[^" + allowed + "]", "-")
             .replaceAll("[-:._]{2,}", "-")
             .replaceAll("(^[-:._]|[-:._]$)", "");
        if (s.length() > max) s = s.substring(0, max);
        if (s.isBlank()) throw new SanitizationException("empty-key");
        return s;
    }

    @Override public boolean supports(URI uri){ return "san".equals(uri.getScheme()) && "key".equals(uri.getHost()); }
    @Override public Sanitizer<?> create(URI uri, Map<String,?> d){
        var q = (uri.getQuery()==null? Map.<String,String>of():
            java.util.Arrays.stream(uri.getQuery().split("&")).map(s->s.split("="))
                .collect(java.util.stream.Collectors.toMap(a->a[0], a->a.length>1?a[1]:"")));
        boolean lower = Boolean.parseBoolean(q.getOrDefault("lower","true"));
        String allowed = q.getOrDefault("allowed","a-z0-9:._-");
        int max = Integer.parseInt(q.getOrDefault("max","128"));
        return new KeySanitizer(lower, allowed, max);
    }
}


