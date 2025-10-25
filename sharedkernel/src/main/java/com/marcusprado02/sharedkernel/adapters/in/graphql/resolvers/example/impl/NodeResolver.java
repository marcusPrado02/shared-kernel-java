package com.marcusprado02.sharedkernel.adapters.in.graphql.resolvers.example.impl;


import org.dataloader.DataLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.stereotype.Controller;

import com.marcusprado02.sharedkernel.contracts.graphql.example.impl.UserAppPort;

import java.util.Optional;
import java.util.UUID;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
@Controller
public class NodeResolver {

    @SchemaMapping(typeName = "Query", field = "node")
    public Object node(@Argument UUID id,
                       DataLoader<UUID, Optional<UserAppPort.UserDTO>> loader) {
        // Exemplo com um único tipo; amplie com type registry
        return loader.load(id).thenApply(opt -> opt.orElse(null));
    }

    @BatchMapping(typeName = "User", field = "id")
    public Map<UserAppPort.UserDTO, String> idBatch(List<UserAppPort.UserDTO> users) {
        var map = new LinkedHashMap<UserAppPort.UserDTO, String>();
        for (var u : users) map.put(u, u.id().toString());
        return map;
    }

}
