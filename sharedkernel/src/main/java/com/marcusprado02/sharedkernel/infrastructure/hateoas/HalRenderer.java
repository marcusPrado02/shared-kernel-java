package com.marcusprado02.sharedkernel.infrastructure.hateoas;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** HAL (_links e _embedded). */
public final class HalRenderer implements LinkRenderer {
    private final ObjectMapper mapper;

    public HalRenderer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override public void renderInto(ObjectNode node, Links links) {
        var _links = node.with("_links");
        links.asMap().forEach((rel, list) -> {
            if (list.size()==1) {
                _links.set(rel, mapper.valueToTree(list.get(0)));
            } else {
                _links.set(rel, mapper.valueToTree(list));
            }
        });
    }
}

