package com.marcusprado02.sharedkernel.crosscutting.helpers.time;

import java.time.Duration;
import java.util.Objects;
import java.util.regex.Pattern;

public final class DurationParser {
  private static final Pattern P = Pattern.compile("(\\d+)(ms|s|m|h|d)");
  private DurationParser(){}

  public static Duration parse(String s) {
    Objects.requireNonNull(s);
    var m = P.matcher(s.replaceAll("\\s+",""));
    if (!m.find()) throw new IllegalArgumentException("invalid duration: "+s);
    long totalMs = 0; int idx = 0;
    while (idx < s.length() && m.find(idx)) {
      long n = Long.parseLong(m.group(1));
      String u = m.group(2);
      totalMs += switch (u) {
        case "ms" -> n;
        case "s"  -> n*1000;
        case "m"  -> n*60*1000;
        case "h"  -> n*60*60*1000;
        case "d"  -> n*24*60*60*1000;
        default -> throw new IllegalStateException();
      };
      idx = m.end();
    }
    return Duration.ofMillis(totalMs);
  }
}
