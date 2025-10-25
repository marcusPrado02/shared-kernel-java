package com.marcusprado02.sharedkernel.cqrs.bulk;

import java.util.List;

import com.marcusprado02.sharedkernel.cqrs.command.Command;

/** Lote heterogêneo: mistura de Commands com o mesmo tipo de resultado R ou Void. */
public record HeterogeneousBulkCommand<R>(List<Command<R>> items, BulkPolicy policy) implements BulkCommand<R> {}
