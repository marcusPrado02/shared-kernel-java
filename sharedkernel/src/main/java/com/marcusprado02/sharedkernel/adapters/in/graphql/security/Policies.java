package com.marcusprado02.sharedkernel.adapters.in.graphql.security;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class Policies {
    @PreAuthorize("hasAuthority('users:read')")
    public void canReadUsers() {}

    @PreAuthorize("hasAuthority('users:write')")
    public void canWriteUsers() {}
}
