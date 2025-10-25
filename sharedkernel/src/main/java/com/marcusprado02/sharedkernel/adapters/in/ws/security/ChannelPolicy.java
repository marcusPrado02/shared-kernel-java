package com.marcusprado02.sharedkernel.adapters.in.ws.security;

import java.util.Set;

public class ChannelPolicy {
    public boolean canSubscribe(String channel, JwtVerifier.Principal p) {
        // Ex.: "prices:*" liberado p/ scope "read:prices"; "orders:account:{id}" exige subject==id
        if (channel.startsWith("prices:")) return p.scopes().contains("read:prices");
        if (channel.startsWith("orders:account:")) {
            var id = channel.substring("orders:account:".length());
            return p.scopes().contains("read:orders") && p.subject().equals(id);
        }
        return false;
    }
    public boolean canPublish(String channel, String op, JwtVerifier.Principal p) {
        if (channel.startsWith("orders:")) return p.scopes().contains("write:orders");
        return false;
    }
}
