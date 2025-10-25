package com.marcusprado02.sharedkernel.infrastructure.maptile.api;

public class AttributionViolationException extends RuntimeException {
    public AttributionViolationException(String msg) { super(msg); }
    public AttributionViolationException(String msg, Throwable cause) { super(msg, cause); }
}