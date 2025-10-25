package com.marcusprado02.sharedkernel.crosscutting.l10n.providers;


import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.marcusprado02.sharedkernel.crosscutting.l10n.BundleStore;
import com.marcusprado02.sharedkernel.crosscutting.l10n.spi.*;

public final class ClasspathJsonBundleStore implements BundleStore, BundleStoreProvider {

    private final String basePath;           // ex.: "i18n/messages"
    private final ConcurrentMap<String, Map<String,String>> cache = new ConcurrentHashMap<>();

    public ClasspathJsonBundleStore(String basePath) { this.basePath = basePath; }

    @Override public Optional<String> getMessage(String tenant, Locale locale, String key) {
        var map = load(tenant, locale);
        return Optional.ofNullable(map.get(key));
    }

    @Override public Set<String> keys(String tenant, Locale locale) {
        return load(tenant, locale).keySet();
    }

    private Map<String,String> load(String tenant, Locale locale) {
        String name = resourceName(tenant, locale);
        return cache.computeIfAbsent(name, n -> {
            try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(n)) {
                if (is == null) return Map.of();
                String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                return parseFlatJson(json); // ver abaixo
            } catch (Exception e) { return Map.of(); }
        });
    }
    private static String resourceName(String tenant, Locale l) {
        if (Locale.ROOT.equals(l)) return "%s_%s.json".formatted("messages", tenant);
        if (!l.getCountry().isEmpty()) {
            return "i18n/messages_%s_%s_%s.json".formatted(tenant, l.getLanguage(), l.getCountry());
        }
        return "i18n/messages_%s_%s.json".formatted(tenant, l.getLanguage());
    }

    /** json plano {"order.created.title":"...", "greeting":"..."} */
    static Map<String,String> parseFlatJson(String json) {
        // Evita dependências: parser extremamente simples para pares "k":"v".
        Map<String,String> map = new HashMap<>();
        json = json.trim();
        if (json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length()-1);
            for (String entry : json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")) {
                String[] kv = entry.split(":(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", 2);
                if (kv.length==2) {
                    String k = kv[0].trim().replaceAll("^\"|\"$", "");
                    String v = kv[1].trim().replaceAll("^\"|\"$", "");
                    v = v.replace("\\n","\n").replace("\\t","\t").replace("\\\"","\"");
                    map.put(k, v);
                }
            }
        }
        return Map.copyOf(map);
    }

    // Provider
    @Override public boolean supports(URI uri){ return "l10n".equals(uri.getScheme()) && "classpath".equals(uri.getHost()); }
    @Override public BundleStore create(URI uri, Map<String, ?> defaults) {
        var q = query(uri);
        String base = q.getOrDefault("base","i18n/messages");
        return new ClasspathJsonBundleStore(base);
    }
    private static Map<String,String> query(URI u){
        if (u.getQuery()==null) return Map.of();
        return Arrays.stream(u.getQuery().split("&")).map(s->s.split("="))
            .collect(java.util.stream.Collectors.toMap(a->a[0], a->a.length>1?a[1]:""));
    }
}
