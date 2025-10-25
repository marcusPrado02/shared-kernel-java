package com.marcusprado02.sharedkernel.crosscutting.formatters.number;

import java.util.Locale;

import com.marcusprado02.sharedkernel.crosscutting.formatters.core.Formatter;

public final class HumanBytesFormatter implements Formatter<Long> {
    private static final String[] UNITS = {"B","KB","MB","GB","TB"};
    @Override
    public String format(Long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes)/Math.log(1024));
        double val = bytes / Math.pow(1024, exp);
        return String.format(Locale.ROOT, "%.2f %s", val, UNITS[exp]);
    }
}

