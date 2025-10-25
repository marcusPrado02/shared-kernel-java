package com.marcusprado02.sharedkernel.infrastructure.geo.api;

public record Address(
    String houseNumber,
    String road,
    String neighbourhood,
    String city,
    String state,
    String stateCode,
    String postcode,
    String country,
    String countryCode
) {}
