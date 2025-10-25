package com.marcusprado02.sharedkernel.contracts.protobuf.example.impl;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface UserAppPort {
    record UserDTO(String id, String name, String email, boolean active,
                   OffsetDateTime createdAt, OffsetDateTime updatedAt, String version) {}

    record QuerySpec(List<Filter> filters, List<SortBy> sort, String cursor, int size, String fields) {
        public record Filter(String field, String op, List<String> values) {}
        public record SortBy(String field, String direction) {}
    }
    record PageResult(String nextCursor, long total, boolean hasMore) {}

    UserDTO create(String name, String email, Optional<String> idempotencyKey);
    UserDTO update(String id, String name, String email, Optional<Boolean> active, Optional<String> ifMatch);
    boolean delete(String id);
    Optional<UserDTO> findById(String id);
    ResultPage search(QuerySpec spec);

    record ResultPage(List<UserDTO> data, PageResult page) {}
    Optional<String> versionOf(String id);
}
