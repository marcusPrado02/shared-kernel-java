package com.marcusprado02.sharedkernel.infrastructure.hateoas;

import java.net.URI;
import java.util.*;
import java.util.function.BiPredicate;

public final class RelRegistry {
    private final Map<String, Rel> rels = new HashMap<>();
    private final List<Curie> curies = new ArrayList<>();

    public Rel register(String rel){ var r = Rel.of(rel); rels.put(rel, r); return r; }
    public Rel curied(String prefix, String name){ return register(prefix + ":" + name); }
    public Rel iana(String std){ return register(std); } // "self", "next", "prev", "collection"
    public List<Curie> curies(){ return List.copyOf(curies); }
    public RelRegistry addCurie(Curie c){ curies.add(c); return this; }
}
