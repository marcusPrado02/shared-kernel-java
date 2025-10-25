package com.marcusprado02.sharedkernel.crosscutting.l10n;

import java.util.*;

public final class FallbackChain {
    /** Ex.: (pt_BR, acme) -> [(pt_BR,acme),(pt_BR,default),(pt,acme),(pt,default),(root,acme),(root,default)] */
    public static List<Pair> sequence(Locale locale, Optional<String> tenant){
        String t = tenant.orElse("default");
        Locale l = locale == null ? Locale.ROOT : locale;
        List<Locale> locales = new ArrayList<>();
        if (!l.equals(Locale.ROOT)) {
            locales.add(l);
            if (!l.getLanguage().isEmpty()) locales.add(new Locale(l.getLanguage()));
        }
        locales.add(Locale.ROOT);
        List<Pair> res = new ArrayList<>();
        for (Locale loc : locales) {
            res.add(new Pair(t, loc));
            if (!"default".equals(t)) res.add(new Pair("default", loc));
        }
        return res;
    }
    public record Pair(String tenant, Locale locale) {}
}
