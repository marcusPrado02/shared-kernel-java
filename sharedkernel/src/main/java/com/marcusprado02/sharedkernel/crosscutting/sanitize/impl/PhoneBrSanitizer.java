package com.marcusprado02.sharedkernel.crosscutting.sanitize.impl;

import java.net.URI;
import java.util.Map;

import com.marcusprado02.sharedkernel.crosscutting.sanitize.core.*;

// Phone BR: mantém dígitos, opcionalmente formata E.164 se tiver DDI
public final class PhoneBrSanitizer implements Sanitizer<String>, SanitizerProvider {
    private final boolean e164;
    public PhoneBrSanitizer(boolean e164){ this.e164=e164; }
    @Override public String sanitize(String in, SanitizationContext ctx) {
        if (in==null) return null;
        String digits = in.replaceAll("\\D", "");
        if (e164) {
            // heurística: se começar com 55, ok; senão prefixa 55
            if (!digits.startsWith("55")) digits = "55" + digits;
            return "+"+digits;
        }
        return digits;
    }
    @Override public boolean supports(URI uri){ return "san".equals(uri.getScheme()) && "phone".equals(uri.getHost()); }
    @Override public Sanitizer<?> create(URI uri, Map<String,?> d){
        var q = (uri.getQuery()==null? Map.<String,String>of():
            java.util.Arrays.stream(uri.getQuery().split("&")).map(s->s.split("="))
                .collect(java.util.stream.Collectors.toMap(a->a[0], a->a.length>1?a[1]:"")));
        boolean e164 = Boolean.parseBoolean(q.getOrDefault("e164","true"));
        return new PhoneBrSanitizer(e164);
    }
}
