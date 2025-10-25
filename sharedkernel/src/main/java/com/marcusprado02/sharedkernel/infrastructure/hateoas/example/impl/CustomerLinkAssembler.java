package com.marcusprado02.sharedkernel.infrastructure.hateoas.example.impl;


import java.net.URI;
import java.util.Map;
import java.util.Set;

import com.marcusprado02.sharedkernel.infrastructure.hateoas.*;

public final class CustomerLinkAssembler extends LinkAssembler<CustomerRes> {

    private final Rel REL_SELF   = registry.iana("self");
    private final Rel REL_EDIT   = registry.curied("fs", "edit");
    private final Rel REL_DELETE = registry.iana("delete");
    private final Rel REL_ORDERS = registry.curied("fs", "orders");
    private final Rel REL_AVATAR = registry.curied("fs", "avatar");

    private final LinkPolicy<CustomerRes> canEdit   = LinkPolicy.<CustomerRes>scope("customer:write")
            .and(LinkPolicy.<CustomerRes>when(c -> !c.deleted()));
    private final LinkPolicy<CustomerRes> canDelete = LinkPolicy.<CustomerRes>scope("customer:admin")
            .and(LinkPolicy.<CustomerRes>when(c -> c.version() > 0));

    public CustomerLinkAssembler(RelRegistry registry){ super(registry); }

    @Override
    protected void buildLinks(CustomerRes c, LinkBuildContext ctx, Links links) {
        links.add(Link.builder(REL_SELF)
                .href(ctx.baseUri().resolve("/v"+ctx.apiVersion()+"/customers/"+c.id()))
                .type("application/json")
                .meta("etag", c.etag())
                .title("This customer")
                .build());

        // Templated link: listar pedidos do cliente com paginação por cursor
        links.add(new TemplateLink(REL_ORDERS,
                "/v"+ctx.apiVersion()+"/orders{?customerId,after,limit}",
                "application/json", "Customer orders", Map.of("templated", true), java.util.List.of()));

        if (canEdit.test(c, ctx)) {
            links.add(Link.builder(REL_EDIT)
                    .href(ctx.baseUri().resolve("/v"+ctx.apiVersion()+"/customers/"+c.id()))
                    .type("application/json")
                    .action(new Action("update", "PUT", URI.create("https://schema.example.com/customer.update.json"),
                            Map.of("if-match", c.etag())))
                    .build());
        }

        if (canDelete.test(c, ctx)) {
            links.add(Link.builder(REL_DELETE)
                    .href(ctx.baseUri().resolve("/v"+ctx.apiVersion()+"/customers/"+c.id()))
                    .type("application/json")
                    .action(new Action("delete", "DELETE", null, Map.of("if-match", c.etag())))
                    .build());
        }

        // Upload de avatar (affordance com precondição e tipo)
        links.add(Link.builder(REL_AVATAR)
                .href(ctx.baseUri().resolve("/v"+ctx.apiVersion()+"/customers/"+c.id()+"/avatar"))
                .type("multipart/form-data")
                .action(new Action("upload", "POST", URI.create("https://schema.example.com/avatar.upload.json"), Map.of()))
                .build());
    }
}