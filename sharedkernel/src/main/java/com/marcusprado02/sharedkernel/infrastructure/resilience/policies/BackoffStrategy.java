package com.marcusprado02.sharedkernel.infrastructure.resilience.policies;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.LongSupplier;

/**
 * BackoffStrategy — versão sênior, genérica e extensível.
 *
 * Convenções:
 *  - attempt é 1-based (1,2,3...). Se receber <1, assume 1.
 *  - NUNCA retorna negativo. Em caso de overflow, satura em Duration.MAX.
 *  - Sempre aplica clamp (min<=delay<=max) quando configurado.
 *  - Métodos estáticos de fábrica para ergonomia; estratégias são imutáveis e thread-safe.
 */
public sealed interface BackoffStrategy
  permits BackoffStrategy.Fixed,
          BackoffStrategy.Exponential,
          BackoffStrategy.ExponentialJitter,
          BackoffStrategy.DecorrelatedJitter,
          BackoffStrategy.Fibonacci,
          BackoffStrategy.Capped,
          BackoffStrategy.DeadlineAware,
          BackoffStrategy.Custom {

  /**
   * Calcula o delay para a tentativa (1-based).
   */
  Duration delay(int attempt);

  // ---------- Fábricas de alto nível ----------

  static BackoffStrategy fixed(Duration delay) {
    return new Fixed(nonNegative(delay));
  }

  static BackoffStrategy exponential(Duration base, double factor) {
    return new Exponential(nonNegative(base), factor, Duration.ZERO, Duration.ofDays(3650)); // clamp amplo por padrão
  }

  static BackoffStrategy exponential(Duration base, double factor, Duration min, Duration max) {
    return new Exponential(nonNegative(base), factor, nonNegative(min), maxPositive(max));
  }

  /** Exponencial com FULL JITTER (AWS/Amazon). delay = random(0, min(cap, base * factor^(n-1))) */
  static BackoffStrategy exponentialFullJitter(Duration base, double factor, Duration cap) {
    return new ExponentialJitter(nonNegative(base), factor, JitterMode.FULL, nonNegative(cap), Duration.ZERO);
  }

  /** Exponencial com EQUAL JITTER (Backoff and Jitter). delay = min(cap, base*exp) /2 + random(0, min(cap, base*exp)/2) */
  static BackoffStrategy exponentialEqualJitter(Duration base, double factor, Duration cap) {
    return new ExponentialJitter(nonNegative(base), factor, JitterMode.EQUAL, nonNegative(cap), Duration.ZERO);
  }

  /** Exponencial com DECORRELATED JITTER (reduz correlação tentativa→tentativa). */
  static BackoffStrategy decorrelatedJitter(Duration base, Duration cap) {
    return new DecorrelatedJitter(nonNegative(base), nonNegative(cap), Duration.ZERO);
  }

  static BackoffStrategy fibonacci(Duration step, Duration min, Duration max) {
    return new Fibonacci(nonNegative(step), nonNegative(min), maxPositive(max));
  }

  /** Impõe teto ao delay de uma estratégia delegada (cap). */
  static BackoffStrategy capped(BackoffStrategy delegate, Duration cap) {
    return new Capped(Objects.requireNonNull(delegate), nonNegative(cap));
  }

  /**
   * Ajusta dinamicamente o delay para não exceder o orçamento de tempo restante (deadline-aware).
   * Útil quando a operação tem “remaining budget” (ex.: SLO guard).
   */
  static BackoffStrategy deadlineAware(BackoffStrategy delegate, LongSupplier remainingBudgetNanosSupplier, Duration minFloor) {
    return new DeadlineAware(Objects.requireNonNull(delegate), Objects.requireNonNull(remainingBudgetNanosSupplier), nonNegative(minFloor));
  }

  /** Estratégia arbitrária via “lambda” (útil para testes/experimentos). */
  static BackoffStrategy custom(BackoffFunction fn) {
    return new Custom(Objects.requireNonNull(fn));
  }

  @FunctionalInterface
  interface BackoffFunction {
    Duration apply(int attempt);
  }

  // ---------- Implementações ----------

  /**
   * FIXED — delay constante.
   */
  final class Fixed implements BackoffStrategy {
    private final Duration delay;
    Fixed(Duration delay) { this.delay = delay; }
    @Override public Duration delay(int attempt) { return delay; }
    @Override public String toString() { return "Fixed(" + delay + ")"; }
  }

  /**
   * EXPONENTIAL — base * factor^(n-1), com clamp [min, max].
   */
  final class Exponential implements BackoffStrategy {
    private final Duration base;
    private final double factor;
    private final Duration min;
    private final Duration max;

    Exponential(Duration base, double factor, Duration min, Duration max) {
      if (factor < 1.0) throw new IllegalArgumentException("factor deve ser >= 1.0");
      this.base = base; this.factor = factor; this.min = min; this.max = max;
    }

    @Override public Duration delay(int attempt) {
      int n = Math.max(1, attempt);
      // valor em nanos com saturação
      long baseNanos = base.toNanos();
      double pow = Math.pow(factor, n - 1);
      long candidate = safeMul(baseNanos, pow);
      long clamped = clamp(candidate, min.toNanos(), max.toNanos());
      return Duration.ofNanos(clamped);
    }

    @Override public String toString() { return "Exponential(base=" + base + ", factor=" + factor + ", min=" + min + ", max=" + max + ")"; }
  }

  /**
   * EXPONENTIAL + JITTER — modos FULL e EQUAL.
   */
  final class ExponentialJitter implements BackoffStrategy {
    private final Duration base;
    private final double factor;
    private final JitterMode mode;
    private final Duration cap;
    private final Duration min;

    ExponentialJitter(Duration base, double factor, JitterMode mode, Duration cap, Duration min) {
      if (factor < 1.0) throw new IllegalArgumentException("factor deve ser >= 1.0");
      this.base = base; this.factor = factor; this.mode = Objects.requireNonNull(mode); this.cap = cap; this.min = min;
    }

    @Override public Duration delay(int attempt) {
      int n = Math.max(1, attempt);
      long expNanos = safeMul(base.toNanos(), Math.pow(factor, n - 1));
      long max = Math.min(expNanos, cap.toNanos());
      long val;
      if (mode == JitterMode.FULL) {
        // random(0, max)
        val = randomBetween(0, max);
      } else {
        // equal jitter: max/2 + random(0, max/2)
        long half = max / 2;
        val = safeAdd(half, randomBetween(0, half));
      }
      long clamped = clamp(val, min.toNanos(), cap.toNanos());
      return Duration.ofNanos(clamped);
    }

    @Override public String toString() { return "ExponentialJitter("+mode+", base="+base+", factor="+factor+", cap="+cap+", min="+min+")"; }
  }

  /**
   * DECORRELATED JITTER — conforme “Exponential Backoff and Jitter (Decorrelated)”.
   * next = random(min, min(cap, prev * 3)); inicia em base.
   */
  final class DecorrelatedJitter implements BackoffStrategy {
    private final Duration base;
    private final Duration cap;
    private final Duration min;
    // State por-thread para evitar shared mutability (decorrelated por fluxo):
    private final ThreadLocal<Long> prevNanos = ThreadLocal.withInitial(() -> 0L);

    DecorrelatedJitter(Duration base, Duration cap, Duration min) {
      this.base = base; this.cap = cap; this.min = min;
    }

    @Override public Duration delay(int attempt) {
      long prev = prevNanos.get();
      long next = (prev == 0L) ? base.toNanos()
                               : randomBetween(min.toNanos(), Math.min(cap.toNanos(), safeMul(prev, 3.0)));
      if (next <= 0) next = min.toNanos();
      prevNanos.set(next);
      return Duration.ofNanos(next);
    }

    @Override public String toString() { return "DecorrelatedJitter(base="+base+", cap="+cap+", min="+min+")"; }
  }

  /**
   * FIBONACCI — delay = step * F(n), com clamp [min,max].
   */
  final class Fibonacci implements BackoffStrategy {
    private final Duration step;
    private final Duration min;
    private final Duration max;

    Fibonacci(Duration step, Duration min, Duration max) {
      this.step = step; this.min = min; this.max = max;
    }

    @Override public Duration delay(int attempt) {
      int n = Math.max(1, attempt);
      long fib = fibSaturated(n); // F(1)=1, F(2)=1, F(3)=2, ...
      long nanos = safeMul(step.toNanos(), fib);
      long clamped = clamp(nanos, min.toNanos(), max.toNanos());
      return Duration.ofNanos(clamped);
    }

    private long fibSaturated(int n) {
      long a = 1, b = 1; // F1=1, F2=1
      if (n <= 2) return 1;
      for (int i = 3; i <= n; i++) {
        long c = safeAdd(a, b);
        a = b; b = c;
      }
      return b;
    }

    @Override public String toString() { return "Fibonacci(step="+step+", min="+min+", max="+max+")"; }
  }

  /**
   * CAPPED — aplica teto ao resultado da estratégia delegada.
   */
  final class Capped implements BackoffStrategy {
    private final BackoffStrategy delegate;
    private final Duration cap;
    Capped(BackoffStrategy delegate, Duration cap) { this.delegate = delegate; this.cap = cap; }
    @Override public Duration delay(int attempt) {
      long v = delegate.delay(attempt).toNanos();
      return Duration.ofNanos(Math.min(v, cap.toNanos()));
    }
    @Override public String toString() { return "Capped(" + delegate + ", cap=" + cap + ")"; }
  }

  /**
   * DEADLINE-AWARE — limita o delay ao orçamento restante (com piso mínimo).
   */
  final class DeadlineAware implements BackoffStrategy {
    private final BackoffStrategy delegate;
    private final LongSupplier remainingBudgetNanosSupplier;
    private final Duration minFloor;

    DeadlineAware(BackoffStrategy delegate, LongSupplier remainingBudgetNanosSupplier, Duration minFloor) {
      this.delegate = delegate;
      this.remainingBudgetNanosSupplier = remainingBudgetNanosSupplier;
      this.minFloor = minFloor;
    }

    @Override public Duration delay(int attempt) {
      long base = delegate.delay(attempt).toNanos();
      long budget = remainingBudgetNanosSupplier.getAsLong();
      long floor = minFloor.toNanos();
      if (budget <= 0) return Duration.ZERO;
      long bounded = Math.max(floor, Math.min(base, budget));
      return Duration.ofNanos(bounded);
    }

    @Override public String toString() { return "DeadlineAware(" + delegate + ", minFloor=" + minFloor + ")"; }
  }

  /**
   * CUSTOM — função arbitrária.
   */
  final class Custom implements BackoffStrategy {
    private final BackoffFunction fn;
    Custom(BackoffFunction fn) { this.fn = fn; }
    @Override public Duration delay(int attempt) { return nonNegative(fn.apply(attempt)); }
    @Override public String toString() { return "Custom(" + fn + ")"; }
  }

  // ---------- Jitter & Utils ----------

  enum JitterMode { FULL, EQUAL }

  private static Duration nonNegative(Duration d) {
    if (d.isNegative()) throw new IllegalArgumentException("Duration negativa: " + d);
    return d;
  }

  private static Duration maxPositive(Duration d) {
    if (d.isNegative() || d.isZero()) return Duration.ofSeconds(1_000_000_000); // ~31 anos
    return d;
  }

  private static long safeMul(long base, double factor) {
    if (base <= 0 || factor <= 0) return 0L;
    double v = base * factor;
    if (v >= Long.MAX_VALUE) return Long.MAX_VALUE;
    return (long) v;
  }

  private static long safeMul(long a, long b) {
    if (a == 0 || b == 0) return 0L;
    if (a > Long.MAX_VALUE / b) return Long.MAX_VALUE;
    return a * b;
  }

  private static long safeAdd(long a, long b) {
    long r = a + b;
    if (((a ^ r) & (b ^ r)) < 0) return Long.MAX_VALUE; // overflow detect
    return r;
  }

  private static long clamp(long v, long min, long max) {
    long lo = Math.min(min, max), hi = Math.max(min, max);
    if (v < lo) return lo;
    if (v > hi) return hi;
    return v;
  }

  private static long randomBetween(long lowInclusive, long highInclusive) {
    if (highInclusive <= lowInclusive) return lowInclusive;
    long bound = (highInclusive - lowInclusive) + 1;
    long r = ThreadLocalRandom.current().nextLong(bound);
    return lowInclusive + r;
  }
}
