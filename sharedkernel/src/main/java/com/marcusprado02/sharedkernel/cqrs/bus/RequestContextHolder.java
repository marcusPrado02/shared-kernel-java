package com.marcusprado02.sharedkernel.cqrs.bus;

import java.util.Optional;

import com.marcusprado02.sharedkernel.cqrs.command.CommandContext;

public final class RequestContextHolder {
  private static final ThreadLocal<CommandContext> TL = new ThreadLocal<>();
  public static void set(CommandContext c){ TL.set(c); }
  public static void clear(){ TL.remove(); }
  public static Optional<CommandContext> get(){ return Optional.ofNullable(TL.get()); }
}