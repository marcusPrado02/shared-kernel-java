package com.marcusprado02.sharedkernel.crosscutting.formatters.text;

import com.marcusprado02.sharedkernel.crosscutting.formatters.core.Formatter;

public final class MaskingFormatter implements Formatter<String> {
    private final int visibleStart, visibleEnd;
    public MaskingFormatter(int visibleStart, int visibleEnd) {
        this.visibleStart = visibleStart; this.visibleEnd = visibleEnd;
    }
    @Override
    public String format(String v) {
        if (v == null || v.length() <= visibleStart+visibleEnd) return "****";
        return v.substring(0, visibleStart) + "****" + v.substring(v.length()-visibleEnd);
    }
}
