package com.marcusprado02.sharedkernel.adapters.in.rest.pagination;
 
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Resultado paginado: itens + metadados + próximo cursor. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PageResult<T>(
        List<T> items,
        boolean hasMore,
        Long totalExact,
        Long totalApprox,
        String nextCursor,
        String prevCursor
) {
    public boolean cursorBased() {
        return nextCursor != null || prevCursor != null;
    }
}
