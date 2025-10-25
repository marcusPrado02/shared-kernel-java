package com.marcusprado02.sharedkernel.adapters.in.rest.dto;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/** RFC-7807 Problem Details. */
public record Problem(
        URI type,
        String title,
        int status,
        String detail,
        String instance,
        @JsonInclude(JsonInclude.Include.NON_NULL) List<Violation> violations,
        @JsonInclude(JsonInclude.Include.NON_NULL) Map<String,Object> ext,
        Instant ts
) implements ResponseDTO {
    public static Problem of(URI type, String title, int status, String detail, String instance,
                             List<Violation> violations, Map<String,Object> ext){
        return new Problem(type, title, status, detail, instance, violations, ext, Instant.now());
    }

    /** Violação de validação (Bean Validation). */
    public record Violation(String field, String message, Object rejectedValue) {}
}
