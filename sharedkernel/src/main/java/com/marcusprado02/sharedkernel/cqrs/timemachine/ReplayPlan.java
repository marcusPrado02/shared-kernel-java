package com.marcusprado02.sharedkernel.cqrs.timemachine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.marcusprado02.sharedkernel.cqrs.bus.EventEnvelope;
import com.marcusprado02.sharedkernel.cqrs.bus.ReplayCursor; // <-- IMPORTA O CURSOR
import com.marcusprado02.sharedkernel.events.integration.Upcaster;

public final class ReplayPlan {

    // ----- Tipos auxiliares -----
    public enum Source { STREAM, ALL_STREAMS }

    /** Escopo de leitura (por stream específico ou all-streams), com range opcional. */
    public record Scope(Source source, String stream, ReplayCursor from, ReplayCursor to) {}

    /** Alvo que receberá eventos durante o replay. */
    public interface Target {
        String name();
        void apply(EventEnvelope evt) throws Exception;
    }

    /** Filtro para selecionar (ou excluir) eventos no replay. */
    public interface Filter {
        boolean test(EventEnvelope evt);
    }

    // ----- Estado imutável do plano -----
    private final Scope scope;
    private final List<Target> targets;
    private final List<Filter> filters;
    private final Upcaster upcaster;
    private final boolean sandbox;

    // ----- Construtor privado usado pelo Builder -----
    private ReplayPlan(Scope scope,
                       List<Target> targets,
                       List<Filter> filters,
                       Upcaster upcaster,
                       boolean sandbox) {
        this.scope    = scope;
        this.targets  = targets;
        this.filters  = filters;
        this.upcaster = upcaster;
        this.sandbox  = sandbox;
    }

    // ----- Builder fluente -----
    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Scope scope;
        private final List<Target> targets = new ArrayList<>();
        private final List<Filter> filters = new ArrayList<>();
        // ⚠ Upcaster precisa bater a assinatura upcast(String,String,Map<String,String>)
        // Aqui definimos identidade: retorna os mesmos campos.
        private Upcaster upcaster = Upcaster.noop();
        private boolean sandbox = true;

        public Builder scope(Scope s)            { this.scope = s; return this; }
        public Builder target(Target t)          { this.targets.add(t); return this; }
        public Builder filter(Filter f)          { this.filters.add(f); return this; }
        public Builder upcaster(Upcaster u)      { this.upcaster = u; return this; }
        public Builder sandbox(boolean value)    { this.sandbox = value; return this; }

        public ReplayPlan build() {
            return new ReplayPlan(
                this.scope,
                List.copyOf(this.targets),
                List.copyOf(this.filters),
                this.upcaster,
                this.sandbox
            );
        }
    }

    // ----- Getters (se precisar acessar de fora) -----
    public Scope scope()                 { return scope; }
    public List<Target> targets()        { return targets; }
    public List<Filter> filters()        { return filters; }
    public Upcaster upcaster()           { return upcaster; }
    public boolean sandbox()             { return sandbox; }
}
