package com.marcusprado02.sharedkernel.adapters.in.rest.dto;

/** Marcador para Response DTOs. */
public interface ResponseDTO {
    /** Versão lógica do contrato (não confundir com versionamento de recurso). */
    default String contractVersion() { return "v1"; }
}
