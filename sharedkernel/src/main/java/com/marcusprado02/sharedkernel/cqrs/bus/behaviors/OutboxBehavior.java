package com.marcusprado02.sharedkernel.cqrs.bus.behaviors;


import java.util.List;
import java.util.concurrent.CompletionStage;

import com.marcusprado02.sharedkernel.cqrs.bus.AsyncCommandBehavior;
import com.marcusprado02.sharedkernel.cqrs.command.*;
import com.marcusprado02.sharedkernel.cqrs.handler.outbox.OutboxService;

public final class OutboxBehavior implements AsyncCommandBehavior {
  private final OutboxService outbox;
  private final PendingOutboxPicker picker;

  public OutboxBehavior(OutboxService outbox, PendingOutboxPicker picker) {
    this.outbox = outbox; this.picker = picker;
  }

  @Override
  public <C extends Command<R>, R> CompletionStage<R> handle(C cmd, CommandContext ctx, Next<C,R> next) {
    return next.proceed(cmd, ctx).whenComplete((r,e) -> {
      if (e == null) {
        var msgs = picker.collect(cmd, ctx);
        if (msgs != null) for (var m : msgs) outbox.append(m.payload(), m.category(), m.key());
      }
    });
  }
}
