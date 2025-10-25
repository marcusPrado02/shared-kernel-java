package com.marcusprado02.sharedkernel.workflow;

import java.time.Clock;
import com.marcusprado02.sharedkernel.workflow.model.WorkflowInstance;

public final class WorkflowDefinition<I, O> {

    private final String name;
    private final Node first;

    private WorkflowDefinition(String name, Node first) {
        this.name = name;
        this.first = first;
    }

    // ✅ usado pelo Engine
    public String name() { return name; }

    // ✅ usado pelo Engine
    public Node firstNode() { return first; }

    /** Contrato do nó inicial (você pode expandir com mais tipos de nós depois). */
    public interface Node {
        void schedule(WorkflowInstance<?> instance, WorkflowStore store, Clock clock);
    }

    // ---------- Builder ----------
    public static <I, O> Builder<I, O> named(String name) { return new Builder<>(name); }

    public static final class Builder<I, O> {
        private final String name;
        private Node first;

        public Builder(String name) { this.name = name; }

        /** Defina o nó inicial (activity/step/timer…) */
        public Builder<I, O> first(Node node) { this.first = node; return this; }

        public WorkflowDefinition<I, O> build() {
            if (first == null) throw new IllegalStateException("first node is required");
            return new WorkflowDefinition<>(name, first);
        }
    }
}
