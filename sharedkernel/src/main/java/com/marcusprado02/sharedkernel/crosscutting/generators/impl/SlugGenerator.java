package com.marcusprado02.sharedkernel.crosscutting.generators.impl;

import java.text.Normalizer;

import com.marcusprado02.sharedkernel.crosscutting.generators.core.*;

public final class SlugGenerator implements Generator<String>, GeneratorProvider {
    @Override public String generate(GenerationContext ctx) {
        Object in = ctx.attributes().getOrDefault("input", "default");
        String s = String.valueOf(in);
        String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        n = n.toLowerCase().replaceAll("[^a-z0-9]+","-").replaceAll("(^-|-$)","");
        return n;
    }
    @Override public boolean supports(java.net.URI uri){ return "gen".equals(uri.getScheme()) && "slug".equals(uri.getHost()); }
    @Override public Generator<?> create(java.net.URI uri, java.util.Map<String,?> d){ return this; }
}
