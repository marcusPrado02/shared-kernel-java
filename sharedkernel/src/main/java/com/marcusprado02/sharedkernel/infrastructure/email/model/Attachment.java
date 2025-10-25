package com.marcusprado02.sharedkernel.infrastructure.email.model;


public record Attachment(String filename, MimeType mimeType, byte[] data, boolean inline, String contentId) {}

