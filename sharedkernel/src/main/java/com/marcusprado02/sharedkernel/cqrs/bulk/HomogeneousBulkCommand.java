package com.marcusprado02.sharedkernel.cqrs.bulk;

import java.util.List;
import com.marcusprado02.sharedkernel.cqrs.command.Command;

public non-sealed class HomogeneousBulkCommand<C extends Command<R>, R> implements BulkCommand<R> {
    private final List<C> items;
    private final BulkPolicy policy;

    public HomogeneousBulkCommand(List<C> items, BulkPolicy policy) {
        this.items = List.copyOf(items);
        this.policy = policy;
    }

    public List<C> items() { return items; }
    public BulkPolicy policy() { return policy; }
}