package com.marcusprado02.sharedkernel.adapters.in.rest.dto.example.impl;

import com.marcusprado02.sharedkernel.adapters.in.rest.dto.RequestDTO;

import jakarta.validation.constraints.*;

import java.util.Set;

/** CreateRequestDTO (somente campos necessários para criação). */
public record CustomerCreateReq(
        @NotBlank(groups = RequestDTO.Create.class) String name,
        @Email(groups = {RequestDTO.Create.class, RequestDTO.Update.class}) String email,
        @Pattern(regexp = "BR|US|EU", groups = RequestDTO.Create.class) String region,
        @Size(max = 3, groups = {RequestDTO.Create.class, RequestDTO.Update.class}) Set<@NotBlank String> tags
) implements RequestDTO {}
