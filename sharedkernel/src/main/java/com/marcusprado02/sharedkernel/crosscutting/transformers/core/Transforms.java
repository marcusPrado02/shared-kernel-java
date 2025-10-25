package com.marcusprado02.sharedkernel.crosscutting.transformers.core;

import java.util.List;

public final class Transforms {

  public static <I,O> TransformFunction<I,O> chain(List<TransformFunction<I, I>> steps, TransformFunction<I,O> terminal) {
      return (in, ctx) -> {
          I current = in;
          for (TransformFunction<I,I> step : steps) {
              var r = step.apply(current, ctx);
              if (r.outcome() != Outcome.OK) return cast(r); // encerrar cedo em DROP/RETRY/DLQ
              current = r.value().orElse(current);
          }
          return terminal.apply(current, ctx);
      };
  }

  public static <I> TransformFunction<I,I> map(java.util.function.BiFunction<I,TransformContext,I> f) {
      return (in, ctx) -> TransformResult.ok(f.apply(in, ctx));
  }

  public static <I> TransformFunction<I,I> filter(java.util.function.BiPredicate<I,TransformContext> p, String reason) {
      return (in, ctx) -> p.test(in, ctx) ? TransformResult.ok(in) : TransformResult.drop(reason);
  }

  public static <I> TransformFunction<I,I> tee(java.util.function.BiConsumer<I,TransformContext> effect) {
      return (in, ctx) -> { effect.accept(in, ctx); return TransformResult.ok(in); };
  }

  public static <I,O> TransformFunction<I,O> recover(TransformFunction<I,O> f, java.util.function.Function<Throwable, TransformResult<O>> handler) {
      return (in, ctx) -> {
          try { return f.apply(in, ctx); } catch (Throwable t) { return handler.apply(t); }
      };
  }

  @SuppressWarnings("unchecked")
  private static <X,Y> TransformResult<Y> cast(TransformResult<X> r) { return (TransformResult<Y>) r; }
}
