package com.marcusprado02.sharedkernel.adapters.in.rest.dto.example.impl;

import java.util.Set;

import com.marcusprado02.sharedkernel.adapters.in.rest.dto.RequestDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** UpdateRequestDTO (PUT/PUT-like). Todos obrigatórios ou opcionais conforme regra. */
public record CustomerUpdateReq(
        @NotBlank(groups = RequestDTO.Update.class) String name,
        @Email(groups = RequestDTO.Update.class) String email,
        @Pattern(regexp = "BR|US|EU", groups = RequestDTO.Update.class) String region,
        @Size(max = 3, groups = RequestDTO.Update.class) Set<@NotBlank String> tags
) implements RequestDTO {}
