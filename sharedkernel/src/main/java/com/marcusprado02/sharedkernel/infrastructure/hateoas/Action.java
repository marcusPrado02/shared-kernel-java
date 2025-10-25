package com.marcusprado02.sharedkernel.infrastructure.hateoas;


import java.net.URI;
import java.util.*;

/** Ação (affordance) vinculada a um link: método, schema e precondições. */
public record Action(
        String name,              // ex.: "update", "cancel", "pay"
        String method,            // GET/POST/PUT/PATCH/DELETE
        URI schema,               // JSON Schema/OpenAPI fragment
        Map<String, Object> preconditions // ex.: { "if-match": "\"W/abc\"" }
) {}
