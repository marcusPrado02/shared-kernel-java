package com.marcusprado02.sharedkernel.crosscutting.sanitize.impl;

import java.net.URI;
import java.util.Map;

import com.marcusprado02.sharedkernel.crosscutting.sanitize.core.*;

public final class FilenameSanitizer implements Sanitizer<String>, SanitizerProvider {
    private final int max;
    public FilenameSanitizer(int max){ this.max=max; }

    @Override public String sanitize(String in, SanitizationContext ctx) {
        if (in==null) return null;
        String s = in.replaceAll("[\\\\/:*?\"<>|]", "_");   // Windows reserved
        s = s.replaceAll("\\p{Cntrl}", "_");
        s = s.replaceAll("\\s+", " ").trim();
        if (s.equals(".") || s.equals("..")) s = "file";
        if (s.length()>max) s = s.substring(0, max);
        return s;
    }

    @Override public boolean supports(URI uri){ return "san".equals(uri.getScheme()) && "filename".equals(uri.getHost()); }
    @Override public Sanitizer<?> create(URI uri, Map<String,?> d){
        var q = (uri.getQuery()==null? Map.<String,String>of():
            java.util.Arrays.stream(uri.getQuery().split("&")).map(s->s.split("="))
                .collect(java.util.stream.Collectors.toMap(a->a[0], a->a.length>1?a[1]:"")));
        int max = Integer.parseInt(q.getOrDefault("max","120"));
        return new FilenameSanitizer(max);
    }
}
