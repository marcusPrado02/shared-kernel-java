package com.marcusprado02.sharedkernel.crosscutting.helpers.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public final class CsvParserHelper {
  private CsvParserHelper(){}
  public static List<List<String>> parse(Reader r) throws IOException {
    List<List<String>> out = new ArrayList<>();
    var br = new BufferedReader(r);
    String line;
    while ((line = br.readLine()) != null) {
      out.add(parseLine(line));
    }
    return out;
  }
  static List<String> parseLine(String line) {
    List<String> cols = new ArrayList<>();
    StringBuilder sb = new StringBuilder();
    boolean quoted = false;
    for (int i=0; i<line.length(); i++) {
      char c = line.charAt(i);
      if (c=='"') {
        if (quoted && i+1<line.length() && line.charAt(i+1)=='"') { sb.append('"'); i++; }
        else { quoted = !quoted; }
      } else if (c==',' && !quoted) { cols.add(sb.toString()); sb.setLength(0); }
      else { sb.append(c); }
    }
    cols.add(sb.toString());
    return cols;
  }
}

