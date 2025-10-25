package com.marcusprado02.sharedkernel.adapters.in.rest.dto;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Set;

/** Projeta um ResponseDTO para os fields solicitados. */
public final class Projector {
    private final ObjectMapper mapper;

    public Projector(ObjectMapper mapper){ this.mapper = mapper; }

    public ObjectNode project(ResponseDTO dto, Set<String> fields){
        var tree = mapper.valueToTree(dto);
        if (fields==null || fields.isEmpty()) return (ObjectNode) tree;
        var obj = (ObjectNode) tree;
        obj.retain(fields);
        // Sempre preservar id/version se existirem
        obj.putIfAbsent("id", obj.get("id"));
        obj.putIfAbsent("version", obj.get("version"));
        return obj;
    }
}
