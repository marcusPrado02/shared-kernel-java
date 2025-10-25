package com.marcusprado02.sharedkernel.saga;

import com.marcusprado02.sharedkernel.cqrs.bus.AsyncCommandBus;
import com.marcusprado02.sharedkernel.cqrs.command.Command;
import com.marcusprado02.sharedkernel.cqrs.command.CommandContext;
import com.marcusprado02.sharedkernel.events.domain.DomainEvent;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public final class ProcessManager {

    private final AsyncCommandBus bus;
    private final SagaStore store;
    private final ProcessedMessageStore pms;

    public ProcessManager(AsyncCommandBus bus, SagaStore store, ProcessedMessageStore pms) {
        this.bus = bus;
        this.store = store;
        this.pms = pms;
    }

    /** Inicia a saga e dispara a ação do primeiro passo. 
     * @throws Exception */
    public <D extends SagaData> String start(SagaDefinition<D> def, D data) throws Exception {
        var sagaId = java.util.UUID.randomUUID().toString();

        // Persiste estado inicial da saga via store (versão 0)
        // Se você tiver uma fábrica SagaInstance.of(...), pode criar e salvar aqui.
        // Mantendo o padrão de mutação via tryUpdateVersion:
        var created = store.tryUpdateVersion(sagaId, 0, x -> {
            @SuppressWarnings("unchecked") var s = (SagaInstance<D>) x;
            s.sagaId = sagaId;
            s.sagaName = def.getSagaName();
            s.data = data;
            s.status = SagaStatus.RUNNING;
            s.currentStep = def.getSteps().get(0).name();
            s.version = 0;
            s.updatedAt = Instant.now();
        });

        // Se não havia registro prévio, alguns stores não criam no tryUpdateVersion.
        // Garanta criação idempotente com save(...) quando necessário:
        if (!created) {
            // Se existir uma fábrica oficial, prefira-a:
            // var s = SagaInstance.of(sagaId, def.getSagaName(), SagaStatus.RUNNING, def.getSteps().get(0).name(), data, 0, OffsetDateTime.now());
            // store.save(s);
            // Como seu modelo parece mutável/public fields, podemos fazer um save inicial mínimo:
            var s = SagaInstance.of(
                sagaId,
                def.getSagaName(),
                SagaStatus.RUNNING,
                def.getSteps().get(0).name(),
                data,
                0,
                java.time.OffsetDateTime.now()
            );
            store.save(s);
        }

        // Dispara ação do primeiro passo
        var firstIdx = 0;
        dispatchAction(def, sagaId, data, firstIdx, null);
        return sagaId;
    }

    /** Trata eventos de retorno; idempotente. 
     * @throws Exception */
    public <D extends SagaData> void onEvent(SagaDefinition<D> def, DomainEvent evt, Class<D> dataType) throws Exception {
        // dedupe at-least-once
        if (pms.seen(evt.id(), def.getSagaName())) return;

        Optional<SagaInstance<D>> opt = store.find(evt.correlationId(), dataType);
        if (opt.isEmpty()) return; // saga finalizada ou desconhecida

        var s = opt.get();
        int idx = indexOfStep(def, s.currentStep);
        var step = def.getSteps().get(idx);

        if (step.onSuccess().test(evt)) {
            advance(def, s, idx + 1, evt);
        } else if (step.onFailure() != null && step.onFailure().test(evt)) {
            compensate(def, s, idx, evt);
        }

        pms.mark(evt.id(), def.getSagaName());
    }

    private <D extends SagaData> void advance(SagaDefinition<D> def, SagaInstance<D> s, int nextIdx, DomainEvent cause) throws Exception {
        if (nextIdx >= def.getSteps().size()) {
            update(s, x -> {
                x.status = SagaStatus.COMPLETED;
                x.currentStep = "DONE";
            });
            return;
        }

        var next = def.getSteps().get(nextIdx);
        update(s, x -> x.currentStep = next.name());
        dispatchAction(def, s.sagaId, s.data, nextIdx, cause);
    }

    private <D extends SagaData> void compensate(SagaDefinition<D> def, SagaInstance<D> s, int idx, DomainEvent cause) throws Exception {
        var step = def.getSteps().get(idx);
        if (step.compensation() == null) {
            update(s, x -> x.status = SagaStatus.FAILED);
            return;
        }
        update(s, x -> x.status = SagaStatus.COMPENSATING);

        Command<?> cmd = step.compensation().apply(s.data);
        var ctx = CommandContext.builder()
                .correlationId(s.sagaId)
                .headers(Map.of("phase", "compensation", "saga", def.getSagaName(), "step", step.name()))
                .build();
        bus.send(cmd, ctx); // fire-and-forget (CompletionStage pode ser observado, se desejar)
    }

    private <D extends SagaData> void dispatchAction(SagaDefinition<D> def, String sagaId, D data, int idx, DomainEvent cause) throws Exception {
        var step = def.getSteps().get(idx);
        Command<?> cmd = step.action().apply(data);

        var ctx = CommandContext.builder()
                .correlationId(sagaId)
                .headers(Map.of("saga", def.getSagaName(), "step", step.name(), "cause", cause == null ? null : cause.id()))
                .build();

        bus.send(cmd, ctx);
        // O scheduler externo deve armar timeout: (sagaId, step.name()) -> step.timeout()
    }

    private static int indexOfStep(SagaDefinition<?> def, String name) {
        var list = def.getSteps();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).name().equals(name)) return i;
        }
        throw new IllegalStateException("Step not found: " + name);
    }

    private <D extends SagaData> void update(SagaInstance<D> s, Consumer<SagaInstance<D>> f) {
        int expected = s.version;
        store.tryUpdateVersion(s.sagaId, expected, x -> {
            @SuppressWarnings("unchecked") var cast = (SagaInstance<D>) x;
            f.accept(cast);
            cast.version = expected + 1;
            cast.updatedAt = Instant.now();
        });
    }
}