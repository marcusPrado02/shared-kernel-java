package com.marcusprado02.sharedkernel.infrastructure.edge.ratelimit;

import java.util.*;

public record RateKey(String tenant, String apiKey, String ip, String route, String method) {
    public String asString() {
        return String.join("|",
            Optional.ofNullable(tenant).orElse("-"),
            Optional.ofNullable(apiKey).orElse("-"),
            Optional.ofNullable(ip).orElse("-"),
            Optional.ofNullable(route).orElse("-"),
            Optional.ofNullable(method).orElse("-"));
    }
}
