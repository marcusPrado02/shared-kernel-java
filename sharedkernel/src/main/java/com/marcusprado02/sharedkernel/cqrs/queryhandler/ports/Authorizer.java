package com.marcusprado02.sharedkernel.cqrs.queryhandler.ports;


import java.util.Set;

public interface Authorizer {
    boolean hasAll(String userId, Set<String> permissions);
}
