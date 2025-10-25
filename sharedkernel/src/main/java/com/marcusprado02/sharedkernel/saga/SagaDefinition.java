package com.marcusprado02.sharedkernel.saga;

import com.marcusprado02.sharedkernel.cqrs.command.Command;
import com.marcusprado02.sharedkernel.events.domain.DomainEvent;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Definição de uma Saga com passos (Step) e um builder fluente.
 * D = tipo do estado/dados da saga (deve implementar SagaData).
 */
public final class SagaDefinition<D extends SagaData> {

    // ====================== STEP ======================
    public static final class Step<D extends SagaData> {
        private final String name;
        private Function<D, Command<?>> action;
        private Function<D, Command<?>> compensation;
        private Predicate<DomainEvent> onSuccess;             // evento esperado
        private Predicate<DomainEvent> onFailure;             // evento de erro
        private RetryPolicy retry = RetryPolicy.linear(3, Duration.ofSeconds(2));
        private Duration timeout = Duration.ofSeconds(30);

        public Step(String name) { this.name = Objects.requireNonNull(name, "step name"); }

        public String name() { return name; }
        public Function<D, Command<?>> action() { return action; }
        public Function<D, Command<?>> compensation() { return compensation; }
        public Predicate<DomainEvent> onSuccess() { return onSuccess; }
        public Predicate<DomainEvent> onFailure() { return onFailure; }
        public RetryPolicy retry() { return retry; }
        public Duration timeout() { return timeout; }
    }

    // ====================== DEFINITION ======================
    private final String sagaName;
    private final List<Step<D>> steps;

    public SagaDefinition(String sagaName, List<Step<D>> steps) {
        this.sagaName = Objects.requireNonNull(sagaName, "sagaName");
        this.steps = List.copyOf(Objects.requireNonNull(steps, "steps"));
    }

    public String getSagaName() { return sagaName; }
    public List<Step<D>> getSteps() { return steps; }

    // ====================== BUILDER ======================
    public static <D extends SagaData> Builder<D> builder(String sagaName) {
        return new Builder<>(sagaName);
    }

    public static final class Builder<D extends SagaData> {
        private final String name;
        private final List<Step<D>> steps = new ArrayList<>();

        public Builder(String name) { this.name = Objects.requireNonNull(name, "saga name"); }

        /** Inicia a definição de um passo. */
        public StepBuilder<D> step(String stepName) { return new StepBuilder<>(this, stepName); }

        /** Conclui a definição e constroi a SagaDefinition. */
        public SagaDefinition<D> build() { return new SagaDefinition<>(name, steps); }

        // ---------- StepBuilder encadeável ----------
        public static final class StepBuilder<D extends SagaData> {
            private final Builder<D> parent;
            private final Step<D> s;

            private StepBuilder(Builder<D> parent, String stepName) {
                this.parent = parent;
                this.s = new Step<>(stepName);
            }

            /** Define o comando de ação principal do passo. */
            public StepBuilder<D> action(Function<D, Command<?>> fn) {
                s.action = Objects.requireNonNull(fn, "action");
                return this;
            }

            /** Define o comando de compensação (rollback) do passo. */
            public StepBuilder<D> compensate(Function<D, Command<?>> fn) {
                s.compensation = Objects.requireNonNull(fn, "compensation");
                return this;
            }

            /** Predicado para identificar o evento de sucesso do passo. */
            public StepBuilder<D> onSuccess(Predicate<DomainEvent> p) {
                s.onSuccess = Objects.requireNonNull(p, "onSuccess");
                return this;
            }

            /** Predicado para identificar o evento de falha do passo. */
            public StepBuilder<D> onFailure(Predicate<DomainEvent> p) {
                s.onFailure = Objects.requireNonNull(p, "onFailure");
                return this;
            }

            /** Política de retry do passo. */
            public StepBuilder<D> retry(RetryPolicy r) {
                s.retry = Objects.requireNonNull(r, "retry");
                return this;
            }

            /** Timeout do passo. */
            public StepBuilder<D> timeout(Duration t) {
                s.timeout = Objects.requireNonNull(t, "timeout");
                return this;
            }

            /** Finaliza o passo atual e retorna ao builder da saga. */
            public Builder<D> end() {
                parent.steps.add(s);
                return parent;
            }
        }
    }
}
