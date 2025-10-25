package com.marcusprado02.sharedkernel.domain.policy.enforcement;


import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import com.marcusprado02.sharedkernel.domain.policy.Decision;
import com.marcusprado02.sharedkernel.domain.policy.EvalContext;
import com.marcusprado02.sharedkernel.domain.policy.Policy;
import com.marcusprado02.sharedkernel.domain.policy.PolicyCache;
import com.marcusprado02.sharedkernel.domain.policy.PolicyRegistry;
import com.marcusprado02.sharedkernel.domain.policy.PolicyResult;
import com.marcusprado02.sharedkernel.domain.service.time.ClockProvider;

public final class DefaultPolicyHandler implements PolicyHandler {

  private final PolicyRegistry registry;
  private final PolicyCache cache;
  private final DecisionMapper mapper;
  private final List<ObligationHandler> obligationHandlers;
  private final List<AdviceHandler> adviceHandlers;
  private final PolicyDecisionProvider provider;
  private final CircuitBreaker breaker;
  private final ClockProvider clock;

  public DefaultPolicyHandler(
      PolicyRegistry registry,
      PolicyCache cache,
      DecisionMapper mapper,
      List<ObligationHandler> obligationHandlers,
      List<AdviceHandler> adviceHandlers,
      PolicyDecisionProvider provider,
      CircuitBreaker breaker,
      ClockProvider clock) {
    this.registry = Objects.requireNonNull(registry);
    this.cache = Objects.requireNonNull(cache);
    this.mapper = Objects.requireNonNull(mapper);
    this.obligationHandlers = List.copyOf(obligationHandlers);
    this.adviceHandlers = List.copyOf(adviceHandlers);
    this.provider = Objects.requireNonNull(provider);
    this.breaker = Objects.requireNonNull(breaker);
    this.clock = Objects.requireNonNull(clock);
  }

  @Override
  public <T> T enforce(String policyId, String version, EvalContext ctx,
                       Supplier<T> action, ResponseFilter<T> responseFilter)
      throws PolicyDeniedException {

    // 1) Decisão (cache + breaker)
    String cacheKey = ctx.tenant + "|" + policyId + "|" + version + "|" + ctx.action + "|" + ctx.resource
        + "|" + Integer.toHexString(ctx.attrs.hashCode());
    Policy policy = registry.get(ctx.tenant, policyId, version);

    PolicyResult result = cache.getOrCompute(cacheKey, () -> {
      if (!breaker.allow())
        return PolicyResult.of(Decision.NOT_APPLICABLE, policy.id(), policy.version(), "breaker-open");
      try {
        var r = provider.evaluate(policy, ctx);
        breaker.onSuccess();
        return r;
      } catch (RuntimeException ex) {
        breaker.onFailure();
        // fallback: default deny ou not_applicable conforme sua estratégia:
        return PolicyResult.of(Decision.DENY, policy.id(), policy.version(), "evaluation-error:"+ex.getClass().getSimpleName());
      }
    });

    // 2) Mapeamento de decisão (pode lançar PolicyDeniedException)
    mapper.onDecision(result);

    // 3) Obligations (efeitos colaterais sincronizados)
    for (var o : result.obligations) {
      for (var h : obligationHandlers) if (h.supports(o)) h.handle(o, ctx, result);
    }
    // 4) Advice (efeitos suaves)
    for (var a : result.advice) {
      for (var h : adviceHandlers) if (h.supports(a)) h.handle(a, ctx, result);
    }

    // 5) Executa ação e filtra resposta (pós-enforcement)
    T raw = action.get();
    return responseFilter.apply(raw, result);
  }
}
