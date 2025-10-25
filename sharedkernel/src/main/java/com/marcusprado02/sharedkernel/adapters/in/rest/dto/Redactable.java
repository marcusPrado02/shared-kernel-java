package com.marcusprado02.sharedkernel.adapters.in.rest.dto;

/** Máscara de dados sensíveis no Response. */
public interface Redactable {
    /** Retorna uma cópia com campos sensíveis mascarados. */
    ResponseDTO redacted();
}
