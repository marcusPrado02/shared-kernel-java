package com.marcusprado02.sharedkernel.cqrs.bus.behaviors;
import java.util.concurrent.*;

import com.marcusprado02.sharedkernel.cqrs.bus.AsyncCommandBehavior;
import com.marcusprado02.sharedkernel.cqrs.command.*;
import com.marcusprado02.sharedkernel.cqrs.handler.UnitOfWork;
import com.marcusprado02.sharedkernel.cqrs.handler.tx.TransactionManager;

public final class UnitOfWorkBehavior implements AsyncCommandBehavior {
  private final TransactionManager tx;

  public UnitOfWorkBehavior(TransactionManager tx) { this.tx = tx; }

  @Override
  public <C extends Command<R>, R> CompletionStage<R> handle(C cmd, CommandContext ctx, Next<C,R> next) {
    UnitOfWork uow = tx.newUnitOfWork();
    try {
      uow.begin();
      return next.proceed(cmd, ctx).whenComplete((r,e) -> {
        if (e == null) { uow.commit(); } else { uow.rollback(); }
      });
    } catch (RuntimeException ex) {
      uow.rollback(); throw ex;
    }
  }
}