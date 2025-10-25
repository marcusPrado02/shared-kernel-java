package com.marcusprado02.sharedkernel.crosscutting.formatters.text;

import java.text.Normalizer;
import java.util.Locale;

import com.marcusprado02.sharedkernel.crosscutting.formatters.core.Formatter;

public final class SlugFormatter implements Formatter<String> {
    @Override
    public String format(String v) {
        String n = Normalizer.normalize(v, Normalizer.Form.NFD);
        n = n.replaceAll("\\p{InCombiningDiacriticalMarks}+","");
        n = n.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+","-");
        return n.replaceAll("(^-|-$)","");
    }
}

