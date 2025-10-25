package com.marcusprado02.sharedkernel.domain.events.model;

public record Content(byte[] data, String contentType, String schemaId) {
    public static Content json(byte[] d, String schemaId) { return new Content(d, "application/json", schemaId); }
}
