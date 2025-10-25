package com.marcusprado02.sharedkernel.crosscutting.sanitize.impl;


import java.net.URI;
import java.util.Map;

import com.marcusprado02.sharedkernel.crosscutting.sanitize.core.*;

public final class HtmlTextOnlySanitizer implements Sanitizer<String>, SanitizerProvider {
    @Override public String sanitize(String input, SanitizationContext ctx) {
        if (input == null) return null;
        // Remove tudo que parece tag/entidade suspeita (abordagem pragmática sem deps)
        String s = input.replaceAll("(?is)<[^>]*>", "");        // strip tags
        s = s.replaceAll("(?is)javascript:", "");               // strip protocols
        s = s.replaceAll("&\\s*{", "");                         // edge
        return s;
    }
    @Override public boolean supports(URI uri){ return "san".equals(uri.getScheme()) && "html".equals(uri.getHost()); }
    @Override public Sanitizer<?> create(URI uri, Map<String,?> d){ return this; }
}
