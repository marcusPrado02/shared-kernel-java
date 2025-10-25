package com.marcusprado02.sharedkernel.domain.snapshot.model;

/** Representa o estado serializado do agregado. */
public record SnapshotPayload(byte[] data, String contentType, String encoding) {}
