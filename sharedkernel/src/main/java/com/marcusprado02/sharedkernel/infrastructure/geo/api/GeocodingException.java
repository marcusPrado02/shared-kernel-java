package com.marcusprado02.sharedkernel.infrastructure.geo.api;

public class GeocodingException extends RuntimeException {
    public enum Code { PROVIDER_UNAVAILABLE, RATE_LIMIT, TIMEOUT, BAD_REQUEST, UNAUTHORIZED, UNKNOWN }
    private final Code code;
    private final String provider;
    public GeocodingException(Code code, String provider, String msg, Throwable cause) {
        super(msg, cause); this.code=code; this.provider=provider;
    }
    public Code code(){ return code; }
    public String provider(){ return provider; }
}
