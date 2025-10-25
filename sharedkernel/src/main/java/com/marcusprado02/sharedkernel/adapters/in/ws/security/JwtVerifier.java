package com.marcusprado02.sharedkernel.adapters.in.ws.security;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.util.annotation.Nullable;

public class JwtVerifier {
    private final Algorithm alg; private final String expectedIssuer;
    public record Principal(String subject, String tenant, java.util.Set<String> scopes) {}

    public JwtVerifier(String secret, String expectedIssuer) {
        this.alg = Algorithm.HMAC256(secret);
        this.expectedIssuer = expectedIssuer;
    }

    public @Nullable Principal verify(ServerHttpRequest req) {
        // Token via "Sec-WebSocket-Protocol: bearer,<jwt>" OU "Authorization: Bearer <jwt>" OU ?token=
        String tok = extractToken(req);
        if (tok == null) return null;
        var dec = JWT.require(alg).withIssuer(expectedIssuer).build().verify(tok);
        String sub = dec.getSubject();
        String tenant = dec.getClaim("tenant").asString(); if (tenant == null) tenant = "default";
        var scopes = java.util.Set.copyOf(java.util.Arrays.asList(dec.getClaim("scope").asString().split(" ")));
        return new Principal(sub, tenant, scopes);
    }

    private String extractToken(ServerHttpRequest req) {
        var proto = req.getHeaders().getFirst("Sec-WebSocket-Protocol");
        if (proto != null && proto.startsWith("bearer,")) return proto.substring("bearer,".length()).trim();
        var auth = req.getHeaders().getFirst("Authorization");
        if (auth != null && auth.toLowerCase().startsWith("bearer ")) return auth.substring(7);
        return req.getQueryParams().getFirst("token");
    }
}
