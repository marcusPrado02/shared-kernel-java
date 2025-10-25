package com.marcusprado02.sharedkernel.crosscutting.parser.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcusprado02.sharedkernel.crosscutting.parser.core.*;

// data/JsonNodeParser.java  (Jackson)
public final class JsonNodeParser implements Parser<JsonNode> {
    private static final ObjectMapper M = new ObjectMapper();
    private final int maxBytes;
    public JsonNodeParser(int maxBytes) { this.maxBytes = maxBytes; }
    @Override public ParseResult<JsonNode> parse(String s) {
        try {
            if (s.length() > maxBytes) return ParseResult.err(ParseError.simple("JSON too large"));
            return ParseResult.ok(M.readTree(s));
        } catch (Exception e) {
            return ParseResult.err(ParseError.of("Invalid JSON", findPos(e), sample(s), "Valide chaves/aspas/virgulas", e));
        }
    }
    private String sample(String s){ return s == null ? "null" : s.substring(0, Math.min(120, s.length())); }
    private int findPos(Exception e){ return -1; }
}

