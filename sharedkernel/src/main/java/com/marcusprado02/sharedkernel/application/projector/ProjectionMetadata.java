package com.marcusprado02.sharedkernel.application.projector;

public final class ProjectionMetadata {
    public static ProjectionEventMeta extract(Object event) {
        // Estratégias: interface comum, anotação ou envelope de transporte.
        if (event instanceof HasEventMeta m)
            return new ProjectionEventMeta(m.eventId(), m.sequence(), m.type());
        throw new IllegalArgumentException("Evento não possui metadados: " + event.getClass());
    }
    public interface HasEventMeta {
        String eventId();
        long sequence();
        String type();
    }
}
