package com.marcusprado02.sharedkernel.adapters.in.rest.dto;


import com.fasterxml.jackson.annotation.*;
import java.util.List;

/** Envelope para coleções (com ambos modos de paginação). */
public record ApiListEnvelope<T extends ResponseDTO>(
        List<T> data,
        Meta meta,
        Links links,
        @JsonInclude(JsonInclude.Include.NON_NULL) PageOffset page,
        @JsonInclude(JsonInclude.Include.NON_NULL) PageCursor cursor
) implements ResponseDTO {}