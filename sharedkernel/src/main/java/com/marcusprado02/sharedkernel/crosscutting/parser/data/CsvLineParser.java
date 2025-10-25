package com.marcusprado02.sharedkernel.crosscutting.parser.data;

import com.marcusprado02.sharedkernel.crosscutting.parser.core.*;

// data/CsvLineParser.java  // 1 linha → lista de colunas
public final class CsvLineParser implements Parser<java.util.List<String>> {
    @Override public ParseResult<java.util.List<String>> parse(String line) {
        try {
            var cols = new java.util.ArrayList<String>();
            var sb = new StringBuilder(); boolean q=false;
            for (int i=0;i<line.length();i++) {
                char c=line.charAt(i);
                if (c=='"') { if (q && i+1<line.length() && line.charAt(i+1)=='"') { sb.append('"'); i++; } else q=!q; }
                else if (c==',' && !q) { cols.add(sb.toString()); sb.setLength(0); }
                else sb.append(c);
            }
            cols.add(sb.toString());
            return ParseResult.ok(java.util.Collections.unmodifiableList(cols));
        } catch (Exception e) { return ParseResult.err(ParseError.of("Invalid CSV line", 0, line, "Cheque aspas/delimitadores", e)); }
    }
}
