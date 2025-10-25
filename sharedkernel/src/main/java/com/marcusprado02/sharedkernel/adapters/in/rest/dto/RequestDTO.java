package com.marcusprado02.sharedkernel.adapters.in.rest.dto;


/** Marcador para Request DTOs. Use grupos de validação por caso de uso. */
public interface RequestDTO {
    interface Create {}
    interface Update {}
    interface Patch {} // ex.: JSON Patch ou Merge Patch
}
