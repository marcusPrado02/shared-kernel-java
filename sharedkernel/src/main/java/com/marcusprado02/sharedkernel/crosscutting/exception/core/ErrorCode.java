package com.marcusprado02.sharedkernel.crosscutting.exception.core;

public enum ErrorCode {
    VALIDATION_ERROR(400, "VALIDATION_ERROR", "Validation failed"),
    AUTH_REQUIRED(401, "AUTH_REQUIRED", "Authentication required"),
    FORBIDDEN(403, "FORBIDDEN", "Not allowed"),
    NOT_FOUND(404, "NOT_FOUND", "Resource not found"),
    CONFLICT(409, "CONFLICT", "Conflict"),
    RATE_LIMITED(429, "RATE_LIMITED", "Too many requests"),
    SERVICE_UNAVAILABLE(503, "SERVICE_UNAVAILABLE", "Service unavailable"),
    INTERNAL_ERROR(500, "INTERNAL_ERROR", "Internal error");

    public final int status; public final String code; public final String title;
    ErrorCode(int s, String c, String t){status=s;code=c;title=t;}
}

