package com.marcusprado02.sharedkernel.adapters.in.rest.dto;

/** Links HATEOAS simples (opcional, leve). */
public record Links(
        String self,
        String first,
        String prev,
        String next,
        String last
) {}

