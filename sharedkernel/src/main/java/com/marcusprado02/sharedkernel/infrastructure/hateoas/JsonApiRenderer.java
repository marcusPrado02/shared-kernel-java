package com.marcusprado02.sharedkernel.infrastructure.hateoas;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** JSON:API (links no topo + related/self). */
public final class JsonApiRenderer implements LinkRenderer {
    @Override public void renderInto(ObjectNode node, Links links) {
        var mapper = new ObjectMapper();
        var linksNode = node.with("links");
        links.asMap().forEach((rel, list) -> {
            // JSON:API pede map simples rel->href (sem muita meta); se precisar, use ext.
            Object first = list.get(0);
            var href = (first instanceof Link l) ? l.href().toString()
                    : ((TemplateLink) first).hrefTemplate();
            linksNode.put(rel, href);
        });
    }
}