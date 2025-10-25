package com.marcusprado02.sharedkernel.infrastructure.sms.core;

import com.marcusprado02.sharedkernel.infrastructure.sms.model.Encoding;

public final class Segmenter {
    private Segmenter() {}
    public static Encoding detectEncoding(String text, Encoding forced) {
        if (forced != null) return forced;
        return isGsm7(text) ? Encoding.GSM7 : Encoding.UCS2;
    }
    public static int segmentCount(String text, Encoding enc) {
        // Regras clássicas: GSM7 160/153; UCS2 70/67
        int single = enc == Encoding.GSM7 ? 160 : 70;
        int multi  = enc == Encoding.GSM7 ? 153 : 67;
        int len = lengthConsideringEscapes(text, enc);
        if (len <= single) return 1;
        return (int)Math.ceil((double)len / multi);
    }
    private static boolean isGsm7(String t){ /* verificação básica de charset GSM-7 */ return t.chars().allMatch(Segmenter::isGsm7Char); }
    private static boolean isGsm7Char(int c){ return c >= 0x20 && c <= 0x7E || "€^{}\\[~]|".indexOf(c) >= 0; }
    private static int lengthConsideringEscapes(String t, Encoding e){ return t.length(); }
}
