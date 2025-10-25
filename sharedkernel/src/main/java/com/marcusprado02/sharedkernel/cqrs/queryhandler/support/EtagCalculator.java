package com.marcusprado02.sharedkernel.cqrs.queryhandler.support;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public final class EtagCalculator {
    public static String weak(Object value){
        try {
            var md = MessageDigest.getInstance("SHA-256");
            var b = md.digest(String.valueOf(value).getBytes(StandardCharsets.UTF_8));
            return "W/\"" + HexFormat.of().formatHex(b, 0, 8) + "\"";
        } catch (Exception e) { return "W/\"na\""; }
    }
}