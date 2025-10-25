package com.marcusprado02.sharedkernel.crosscutting.parser.net;

import com.marcusprado02.sharedkernel.crosscutting.parser.core.ParseError;
import com.marcusprado02.sharedkernel.crosscutting.parser.core.ParseResult;
import com.marcusprado02.sharedkernel.crosscutting.parser.core.Parser;

public final class CidrParser implements Parser<Cidr> {
    @Override public ParseResult<Cidr> parse(String s) {
        try {
            var p = s.split("/");
            var ip = java.net.InetAddress.getByName(p[0]);
            int prefix = Integer.parseInt(p[1]);
            if (prefix < 0 || prefix > (ip.getAddress().length==4 ? 32 : 128))
                return ParseResult.err(ParseError.simple("Invalid prefix length"));
            return ParseResult.ok(new Cidr(ip, prefix));
        } catch (Exception e) {
            return ParseResult.err(ParseError.of("Invalid CIDR", 0, s, "Ex.: 10.0.0.0/8", e));
        }
    }
}
