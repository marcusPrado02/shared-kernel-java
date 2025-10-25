package com.marcusprado02.sharedkernel.crosscutting.context;

import java.util.List;
import java.util.Map;

public record AuthContext(String subjectId, List<String> roles, Map<String,Object> claims) {
    public boolean hasRole(String role) { return roles != null && roles.contains(role); }
}