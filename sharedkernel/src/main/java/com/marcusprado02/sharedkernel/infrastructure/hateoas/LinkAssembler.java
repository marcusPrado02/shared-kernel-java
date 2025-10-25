package com.marcusprado02.sharedkernel.infrastructure.hateoas;


import java.net.URI;
import java.util.*;
import java.util.function.BiConsumer;

public abstract class LinkAssembler<T> {

    protected final RelRegistry registry;

    protected LinkAssembler(RelRegistry registry){ this.registry = registry; }

    /** Implementação específica monta links de um recurso. */
    protected abstract void buildLinks(T resource, LinkBuildContext ctx, Links links);

    /** Gancho para links de coleção/paginação. */
    protected void buildCollectionLinks(Collection<?> page, LinkBuildContext ctx, Links links){}

    /** Helper para criar Link resolvido a partir de path relativo. */
    protected Link link(Rel rel, LinkBuildContext ctx, String relativePath){
        URI href = ctx.baseUri().resolve(relativePath);
        return Link.builder(rel).href(href).build();
    }

    /** Helper para templated. */
    protected TemplateLink templated(Rel rel, String hrefTemplate){
        return new TemplateLink(rel, hrefTemplate, "application/json", null, Map.of("templated", true), List.of());
    }

    public Links forItem(T resource, LinkBuildContext ctx){
        Links links = new Links();
        buildLinks(resource, ctx, links);
        return links;
    }

    public Links forCollection(Collection<?> page, LinkBuildContext ctx){
        Links links = new Links();
        buildCollectionLinks(page, ctx, links);
        return links;
    }
}