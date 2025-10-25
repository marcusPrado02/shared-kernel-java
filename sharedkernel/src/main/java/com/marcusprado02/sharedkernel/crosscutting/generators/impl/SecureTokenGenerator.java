package com.marcusprado02.sharedkernel.crosscutting.generators.impl;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

import com.marcusprado02.sharedkernel.crosscutting.generators.core.*;

public final class SecureTokenGenerator implements Generator<String>, GeneratorProvider {
    private final int numBytes;
    private final SecureRandom sr;
    public SecureTokenGenerator(int numBytes){ this.numBytes=numBytes; this.sr=new SecureRandom(); }

    @Override public String generate(GenerationContext ctx) {
        byte[] b = new byte[numBytes];
        if (ctx.deterministicSeed().isPresent()) {
            var rng = ctx.rng(); rng.nextBytes(b);
        } else sr.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    @Override public boolean supports(java.net.URI uri){ return "gen".equals(uri.getScheme()) && "token".equals(uri.getHost()); }
    @Override public Generator<?> create(java.net.URI uri, Map<String,?> defaults) {
        var q = uri.getQuery()==null?Map.<String,String>of():java.util.Arrays.stream(uri.getQuery().split("&"))
                .map(s->s.split("=")).collect(java.util.stream.Collectors.toMap(a->a[0], a->a[1]));
        int n = Integer.parseInt(q.getOrDefault("bytes", "32"));
        return new SecureTokenGenerator(n);
    }
}
