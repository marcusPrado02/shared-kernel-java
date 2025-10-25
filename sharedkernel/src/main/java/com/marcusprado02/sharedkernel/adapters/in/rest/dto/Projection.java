package com.marcusprado02.sharedkernel.adapters.in.rest.dto;


import com.fasterxml.jackson.annotation.*;
import java.util.*;

/** Field selection (sparse fieldsets) e expansions. */
public record Projection(
        @JsonProperty("fields") Set<String> fields,
        @JsonProperty("expand") Set<String> expand
) {
    public static Projection none() { return new Projection(Set.of(), Set.of()); }
}
