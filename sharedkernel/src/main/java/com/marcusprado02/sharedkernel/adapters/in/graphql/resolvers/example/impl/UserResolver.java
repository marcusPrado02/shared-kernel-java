package com.marcusprado02.sharedkernel.adapters.in.graphql.resolvers.example.impl;


import io.micrometer.observation.annotation.Observed;
import jakarta.validation.Valid;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.stereotype.Controller;

import com.marcusprado02.sharedkernel.contracts.graphql.QuerySpec;
import com.marcusprado02.sharedkernel.contracts.graphql.example.impl.UserAppPort;

import java.util.*;

@Controller
@Observed(name = "graphql.resolver", contextualName = "UserResolver")
public class UserResolver {

    private final UserAppPort app;

    public UserResolver(UserAppPort app) { this.app = app; }

    @QueryMapping
    public UserAppPort.Connection<UserAppPort.UserDTO> users(
            @Argument List<QuerySpec.Filter> filters,
            @Argument List<QuerySpec.SortBy> sort,
            @Argument QuerySpec.PageRequest page) {
        var spec = new QuerySpec(
                Optional.ofNullable(filters).orElse(List.of()),
                Optional.ofNullable(sort).orElse(List.of()),
                page != null ? page : new QuerySpec.PageRequest(20, null, null, null));
        return app.search(spec);
    }

    @QueryMapping
    public UserAppPort.UserDTO user(@Argument UUID id) {
        return app.findById(id).orElse(null);
    }

    @MutationMapping
    public UserAppPort.UserDTO createUser(@Argument @Valid UserAppPort.UserCreate input,
                                          @Argument Optional<String> idempotencyKey) {
        return app.create(input, idempotencyKey);
    }

    @MutationMapping
    public UserAppPort.UserDTO updateUser(@Argument UUID id,
                                          @Argument @Valid UserAppPort.UserUpdate input,
                                          @Argument Optional<String> ifMatch) {
        return app.update(id, input, ifMatch);
    }

    @MutationMapping
    public Boolean deleteUser(@Argument UUID id) {
        return app.delete(id);
    }
}
