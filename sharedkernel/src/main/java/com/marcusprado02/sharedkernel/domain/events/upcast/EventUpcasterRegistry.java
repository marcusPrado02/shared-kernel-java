package com.marcusprado02.sharedkernel.domain.events.upcast;


import java.util.*;

import com.marcusprado02.sharedkernel.domain.events.model.EventType;

public final class EventUpcasterRegistry {
    private final Map<String, List<EventUpcaster>> byFqn = new HashMap<>();
    
    public void register(EventUpcaster up) {
        byFqn.computeIfAbsent(up.eventType().fqn(), k -> new ArrayList<>()).add(up);
        byFqn.get(up.eventType().fqn()).sort(Comparator.comparingInt(EventUpcaster::fromVersion));
    }

    public List<EventUpcaster> chainFor(EventType type) {
        return byFqn.getOrDefault(type.fqn(), List.of());
    }
}
