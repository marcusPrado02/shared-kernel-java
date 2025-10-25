package com.marcusprado02.sharedkernel.crosscutting.helpers.text;

import java.text.Normalizer;
import java.util.Locale;

public final class Slugify {
  private Slugify(){}
  public static String slug(String s) {
    String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+","");
    n = n.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+","-").replaceAll("(^-|-$)","");
    return n;
  }
}

