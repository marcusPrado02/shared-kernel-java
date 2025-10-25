package com.marcusprado02.sharedkernel.crosscutting.parser.number;

import com.marcusprado02.sharedkernel.crosscutting.parser.core.ParseError;
import com.marcusprado02.sharedkernel.crosscutting.parser.core.ParseResult;
import com.marcusprado02.sharedkernel.crosscutting.parser.core.Parser;

// number/HumanBytesParser.java   // "1.5MB", "1024", "2GiB"
public final class HumanBytesParser implements Parser<Long> {
    @Override public ParseResult<Long> parse(String s) {
        try {
            var t = s.trim().toUpperCase(java.util.Locale.ROOT);
            var m = java.util.regex.Pattern.compile("^([0-9]+(?:\\.[0-9]+)?)\\s*(B|KB|MB|GB|TB|KIB|MIB|GIB|TIB)?$").matcher(t);
            if (!m.find()) return ParseResult.err(ParseError.of("Invalid size", 0, s, "Ex.: 1.5MB, 2GiB, 1024", null));
            double v = Double.parseDouble(m.group(1));
            String u = java.util.Objects.toString(m.group(2), "B");
            double base = (u.endsWith("IB") ? 1024d : 1000d);
            int exp = switch (u) { case "B"->0; case "KB","KIB"->1; case "MB","MIB"->2; case "GB","GIB"->3; case "TB","TIB"->4; default -> 0; };
            long bytes = (long) Math.round(v * Math.pow(base, exp));
            return ParseResult.ok(bytes);
        } catch (Exception e) {
            return ParseResult.err(ParseError.of("Invalid size", 0, s, "Ex.: 512KB, 1.5MB, 2GiB", e));
        }
    }
}

