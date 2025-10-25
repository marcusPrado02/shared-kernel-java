package com.marcusprado02.sharedkernel.cqrs.bus.behaviors;


import java.lang.annotation.*;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import com.marcusprado02.sharedkernel.cqrs.bus.AsyncCommandBehavior;
import com.marcusprado02.sharedkernel.cqrs.command.*;

import com.marcusprado02.sharedkernel.cqrs.handler.security.AuthorizationService;



public final class AuthorizationBehavior implements AsyncCommandBehavior {
  private final AuthorizationService authz;

  public AuthorizationBehavior(AuthorizationService authz) { this.authz = authz; }

  @Override
  public <C extends Command<R>, R> CompletionStage<R> handle(C cmd, CommandContext ctx, Next<C,R> next) {
    var ann = cmd.getClass().getAnnotation(RequiresPermissions.class);
    if (ann == null || ann.value().length == 0) return next.proceed(cmd, ctx);

    var perms = Set.copyOf(Arrays.asList(ann.value()));
    var uid = ctx.userId().orElse(null);
    if (uid == null || !authz.hasAll(uid, perms)) {
      return CompletableFuture.failedStage(new SecurityException("Permissões insuficientes: " + perms));
    }
    return next.proceed(cmd, ctx);
  }
}
