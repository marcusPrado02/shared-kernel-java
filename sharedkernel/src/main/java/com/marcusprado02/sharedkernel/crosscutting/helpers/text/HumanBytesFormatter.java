package com.marcusprado02.sharedkernel.crosscutting.helpers.text;

import java.util.Locale;

public final class HumanBytesFormatter {
  private HumanBytesFormatter(){}
  private static final String[] UNITS = {"B","KB","MB","GB","TB","PB"};
  public static String humanize(long bytes) {
    if (bytes < 1024) return bytes + " B";
    int exp = (int) (Math.log(bytes)/Math.log(1024));
    double val = bytes / Math.pow(1024, exp);
    return String.format(Locale.ROOT, "%.2f %s", val, UNITS[exp]);
  }
}
