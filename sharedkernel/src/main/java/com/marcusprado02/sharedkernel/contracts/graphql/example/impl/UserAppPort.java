package com.marcusprado02.sharedkernel.contracts.graphql.example.impl;


import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.marcusprado02.sharedkernel.contracts.graphql.QuerySpec;

public interface UserAppPort {
    record UserDTO(java.util.UUID id, String name, String email, boolean active,
                   java.time.OffsetDateTime createdAt, java.time.OffsetDateTime updatedAt) {}

    record Connection<T>(java.util.List<Edge<T>> edges, PageInfo pageInfo, long totalCount) {
        public record Edge<T>(T node, String cursor) {}
        public record PageInfo(boolean hasNextPage, boolean hasPreviousPage, String startCursor, String endCursor) {}
    }

    UserDTO create(UserCreate input, Optional<String> idempotencyKey);
    UserDTO update(java.util.UUID id, UserUpdate input, Optional<String> ifMatch);
    boolean delete(java.util.UUID id);
    Optional<UserDTO> findById(java.util.UUID id);
    Connection<UserDTO> search(QuerySpec spec);

    record UserCreate(String name, String email) {}
    record UserUpdate(String name, String email, Boolean active) {}
    Optional<String> versionOf(java.util.UUID id);
}
