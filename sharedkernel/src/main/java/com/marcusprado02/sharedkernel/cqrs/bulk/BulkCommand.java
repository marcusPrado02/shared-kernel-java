package com.marcusprado02.sharedkernel.cqrs.bulk;

import java.util.List;

import com.marcusprado02.sharedkernel.cqrs.command.Command;

/** Representa a intenção de executar um lote de Commands. */
public sealed interface BulkCommand<R> extends Command<BulkResult<R>> permits HomogeneousBulkCommand, HeterogeneousBulkCommand {}

