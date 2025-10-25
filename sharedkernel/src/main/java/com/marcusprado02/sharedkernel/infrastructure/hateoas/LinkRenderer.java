package com.marcusprado02.sharedkernel.infrastructure.hateoas;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface LinkRenderer {
    /** Insere links no nó do DTO já convertido em JSON. */
    void renderInto(ObjectNode dtoJson, Links links);
}
