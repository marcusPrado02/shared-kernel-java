package com.marcusprado02.sharedkernel.cqrs.command;

/** Marca um comando CQRS que produz um resultado do tipo R. */
public interface Command<R> { /* vazio por design: payload puro, sem metadados */ }
