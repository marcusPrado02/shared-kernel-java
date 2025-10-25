package com.marcusprado02.sharedkernel.application.denorm;


import java.util.*;
import java.util.function.Predicate;
import com.marcusprado02.sharedkernel.domain.events.model.EventEnvelope;

public final class DenormPlan {
    private final String collection;
    private final Predicate<EventEnvelope> when;
    private final DenormMapper mapper;
    private final List<Enricher> enrichers;

    public DenormPlan(String collection, Predicate<EventEnvelope> when,
                      DenormMapper mapper, List<Enricher> enrichers) {
        this.collection = collection;
        this.when = when;
        this.mapper = mapper;
        this.enrichers = List.copyOf(enrichers);
    }

    public boolean matches(EventEnvelope e) { return when.test(e); }

    public void execute(DenormSink sink, EventEnvelope e) {
        var doc = mapper.map(e);
        for (var enr : enrichers) doc = enr.apply(doc);
        var id = String.valueOf(doc.get("id")); // regra: todo doc precisa de "id"
        sink.upsert(collection, id, doc);
    }

    public interface DenormMapper {
        Map<String,Object> map(EventEnvelope e);
    }

    public static Builder to(String collection) { return new Builder(collection); }

    public static final class Builder {
        private final String collection;
        private Predicate<EventEnvelope> when = e -> true;
        private DenormMapper mapper;
        private final List<Enricher> enrichers = new ArrayList<>();

        private Builder(String collection){ this.collection = collection; }

        public Builder whenType(String... types) {
            var set = Set.of(types);
            this.when = e -> set.contains(e.metadata().eventType().toString());
            return this;
        }
        public Builder map(DenormMapper mapper){ this.mapper = mapper; return this; }
        public Builder enrich(Enricher enr){ this.enrichers.add(enr); return this; }

        public DenormPlan build(){ return new DenormPlan(collection, when, mapper, enrichers); }
    }
}