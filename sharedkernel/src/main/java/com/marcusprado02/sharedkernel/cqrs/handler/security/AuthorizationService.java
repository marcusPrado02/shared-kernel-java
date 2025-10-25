package com.marcusprado02.sharedkernel.cqrs.handler.security;

import java.util.Set;

public interface AuthorizationService {
    boolean hasAll(String userId, Set<String> permissions);
}