package com.marcusprado02.sharedkernel.crosscutting.generators.impl;

import java.util.regex.*;

import com.marcusprado02.sharedkernel.crosscutting.generators.core.*;

public final class TemplateGenerator implements Generator<String>, GeneratorProvider {
    private final String template;
    private static final Pattern P = Pattern.compile("\\$\\{([a-zA-Z0-9_.-]+)}");
    public TemplateGenerator(String template){ this.template=template; }

    @Override public String generate(GenerationContext ctx) {
        var m = P.matcher(template);
        var sb = new StringBuffer();
        while (m.find()) {
            String key = m.group(1);
            Object v = ctx.attributes().getOrDefault(key, "");
            m.appendReplacement(sb, Matcher.quoteReplacement(String.valueOf(v)));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    @Override public boolean supports(java.net.URI uri){ return "gen".equals(uri.getScheme()) && "template".equals(uri.getHost()); }
    @Override public Generator<?> create(java.net.URI uri, java.util.Map<String,?> defaults) {
        String tpl = java.net.URLDecoder.decode(uri.getPath()!=null?uri.getPath().replaceFirst("/",""):"", java.nio.charset.StandardCharsets.UTF_8);
        return new TemplateGenerator(tpl);
    }
}
