package com.marcusprado02.sharedkernel.crosscutting.parser.net;

import java.net.URI;

import com.marcusprado02.sharedkernel.crosscutting.parser.core.ParseError;
import com.marcusprado02.sharedkernel.crosscutting.parser.core.ParseResult;
import com.marcusprado02.sharedkernel.crosscutting.parser.core.Parser;

// net/UriParser.java
public final class UriParser implements Parser<URI> {
    @Override public ParseResult<URI> parse(String s) {
        try { return ParseResult.ok(new URI(s)); }
        catch (Exception e) { return ParseResult.err(ParseError.of("Invalid URI", 0, s, "Ex.: https://example.com/path?x=1", e)); }
    }
}
