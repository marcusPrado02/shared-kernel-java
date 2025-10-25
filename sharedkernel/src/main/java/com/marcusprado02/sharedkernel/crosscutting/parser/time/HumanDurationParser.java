package com.marcusprado02.sharedkernel.crosscutting.parser.time;

import com.marcusprado02.sharedkernel.crosscutting.parser.core.ParseError;
import com.marcusprado02.sharedkernel.crosscutting.parser.core.ParseResult;
import com.marcusprado02.sharedkernel.crosscutting.parser.core.Parser;
import java.time.Duration;
import java.util.regex.Pattern;

// time/HumanDurationParser.java   // suporta "500ms", "30s", "5m", "2h", "3d", e combinações: "1h30m"
public final class HumanDurationParser implements Parser<Duration> {
    private static final Pattern P = Pattern.compile("(\\d+)(ms|s|m|h|d)");
    @Override public ParseResult<Duration> parse(String s) {
        if (s == null || s.isBlank()) return ParseResult.err(ParseError.simple("Empty duration"));
        var m = P.matcher(s.replaceAll("\\s+",""));
        long totalMs = 0; int idx = 0; boolean matched = false;
        while (m.find(idx)) {
            matched = true;
            long n = Long.parseLong(m.group(1)); String u = m.group(2);
            totalMs += switch (u) { case "ms" -> n; case "s" -> n*1000; case "m" -> n*60_000;
                                     case "h" -> n*3_600_000; case "d" -> n*86_400_000; default -> 0; };
            idx = m.end();
        }
        if (!matched) return ParseResult.err(ParseError.of("Invalid duration", 0, s, "Ex.: 1h30m, 500ms", null));
        return ParseResult.ok(java.time.Duration.ofMillis(totalMs));
    }
}
