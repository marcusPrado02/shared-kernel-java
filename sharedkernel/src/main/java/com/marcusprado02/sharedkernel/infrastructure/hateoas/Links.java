package com.marcusprado02.sharedkernel.infrastructure.hateoas;

import java.net.URI;
import java.util.*;


/** Coleção de links agrupados por rel. */
public final class Links {
    private final Map<String, List<Object>> map = new LinkedHashMap<>();
    public Links add(Link link){ map.computeIfAbsent(link.rel().value(), key -> new ArrayList<>()).add(link); return this; }
    public Links add(TemplateLink link){ map.computeIfAbsent(link.rel().value(), key -> new ArrayList<>()).add(link); return this; }
    public Map<String, List<Object>> asMap(){ return Collections.unmodifiableMap(map); }
}
