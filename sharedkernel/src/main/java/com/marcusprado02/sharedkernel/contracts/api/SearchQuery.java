package com.marcusprado02.sharedkernel.contracts.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record SearchQuery(
        Map<String, List<String>> filters,   // ex: status=[ACTIVE], price[gte]=100
        Optional<String> fields              // ex: "id,name,email"
) {
    public static SearchQuery of(Map<String, List<String>> filters, String fields) {
        return new SearchQuery(filters, Optional.ofNullable(fields).filter(s -> !s.isBlank()));
    }
}
