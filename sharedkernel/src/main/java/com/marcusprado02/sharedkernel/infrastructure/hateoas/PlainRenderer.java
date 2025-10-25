package com.marcusprado02.sharedkernel.infrastructure.hateoas;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Plain (coloca "links": {rel:[...]}) — útil para seus ApiResponseEnvelope. */
public final class PlainRenderer implements LinkRenderer {
    @Override public void renderInto(ObjectNode node, Links links) {
        var mapper = new ObjectMapper();
        node.set("links", mapper.valueToTree(links.asMap()));
    }
}
