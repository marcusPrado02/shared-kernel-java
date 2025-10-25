package com.marcusprado02.sharedkernel.adapters.in.rest.dto.example.impl;

import com.marcusprado02.sharedkernel.adapters.in.rest.dto.ResponseDTO;

/** Representação compacta para referências (embedding com expand=...). */
public record CustomerRef(String id, String name) implements ResponseDTO {}

