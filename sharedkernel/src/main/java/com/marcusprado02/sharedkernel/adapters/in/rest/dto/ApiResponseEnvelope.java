package com.marcusprado02.sharedkernel.adapters.in.rest.dto;

/** Envelope genérico – singular. */
public record ApiResponseEnvelope<T extends ResponseDTO>(
        T data,
        Meta meta,
        Links links
) implements ResponseDTO {}
