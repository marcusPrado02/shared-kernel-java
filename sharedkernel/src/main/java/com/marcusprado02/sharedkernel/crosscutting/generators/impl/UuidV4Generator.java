package com.marcusprado02.sharedkernel.crosscutting.generators.impl;

import java.util.UUID;

import com.marcusprado02.sharedkernel.crosscutting.generators.core.*;

public final class UuidV4Generator implements Generator<String>, GeneratorProvider {
    @Override public String generate(GenerationContext ctx) { return UUID.randomUUID().toString(); }
    // Provider
    @Override public boolean supports(java.net.URI uri){ return uri.getScheme().equals("gen") && uri.getHost().equals("uuid"); }
    @Override public Generator<?> create(java.net.URI uri, java.util.Map<String,?> defaults){ return this; }
}
