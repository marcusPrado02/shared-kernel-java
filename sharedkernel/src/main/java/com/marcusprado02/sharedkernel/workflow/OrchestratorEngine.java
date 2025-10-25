package com.marcusprado02.sharedkernel.workflow;

import java.time.Clock;
import java.util.Map;

import com.marcusprado02.sharedkernel.workflow.model.WorkflowInstance;

public final class OrchestratorEngine {

    private final WorkflowStore store;
    private final ActivityDispatcher dispatcher;
    private final Clock clock;

    public OrchestratorEngine(WorkflowStore store, ActivityDispatcher dispatcher, Clock clock) {
        this.store = store;
        this.dispatcher = dispatcher;
        this.clock = clock;
    }

    public <I,O> String start(WorkflowDefinition<I,O> def, I input) {
        var id = java.util.UUID.randomUUID().toString();
        // depois (usa a sua fábrica correta)
        var inst = WorkflowInstance.newInstance(id, def.name(), input);
        store.update(inst);
        store.appendHistory(id, "Started", Map.of("input", input));
        // dispara nó inicial (schedule do 1º step)
        def.firstNode().schedule(inst, store, clock);
        return id;
    }

    public void signal(String workflowId, String name, Object payload) {
        var inst = store.load(workflowId);
        store.appendHistory(workflowId, "SignalReceived", Map.of("name", name, "payload", payload));
        inst.onSignal(name, payload);
        store.update(inst);
    }

    // loop de execução de atividades (workers separados consomem task-queue)
    public void tickTimers() {
        // encontra timers vencidos e gera eventos TimerFired → movimenta grafo
    }
}

