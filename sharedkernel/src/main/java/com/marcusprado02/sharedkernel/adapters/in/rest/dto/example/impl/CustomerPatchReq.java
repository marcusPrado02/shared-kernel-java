package com.marcusprado02.sharedkernel.adapters.in.rest.dto.example.impl;

import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.marcusprado02.sharedkernel.adapters.in.rest.dto.RequestDTO;

/** JSON Merge Patch (RFC-7386) – campos opcionais, null remove. */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record CustomerPatchReq(
        Optional<String> name,
        Optional<String> email,
        Optional<String> region,
        Optional<Set<String>> tags
) implements RequestDTO {}
