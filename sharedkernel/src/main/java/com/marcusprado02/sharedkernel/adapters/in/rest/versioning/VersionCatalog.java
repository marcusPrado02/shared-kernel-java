package com.marcusprado02.sharedkernel.adapters.in.rest.versioning;


import java.util.*;
import java.util.stream.Collectors;

public final class VersionCatalog {
    private final Map<String, SortedSet<ApiVersion>> supported = new HashMap<>();
    /** Registra representações suportadas por media type lógico (ex.: "customer"). */
    public VersionCatalog register(String logicalType, ApiVersion... versions){
        supported.computeIfAbsent(logicalType, key -> new TreeSet<>()).addAll(Arrays.asList(versions));
        return this;
    }
    public Optional<ApiVersion> best(String logicalType, ApiVersion requested){
        var set = supported.getOrDefault(logicalType, new TreeSet<>());
        if (set.isEmpty()) return Optional.empty();
        // regra: major igual e menor <= solicitado; senão, última minor do mesmo major; senão, maior compatível inferior
        var sameMajor = set.stream().filter(v -> v.major()==requested.major()).toList();
        if (!sameMajor.isEmpty()){
            var lessOrEqualRequestedMinor = sameMajor.stream().filter(v -> v.minor()<=requested.minor()).max(ApiVersion::compareTo);
            if (lessOrEqualRequestedMinor.isPresent()) return lessOrEqualRequestedMinor;
            return sameMajor.stream().max(ApiVersion::compareTo);
        }
        // fallback: maior major inferior ao solicitado
        var lowerMajor = set.stream().filter(v -> v.major()<requested.major()).max(ApiVersion::compareTo);
        return lowerMajor.isPresent()? lowerMajor : Optional.of(set.last());
    }
    public ApiVersion latest(String logicalType){ return supported.get(logicalType).last(); }
    public boolean supports(String logicalType, ApiVersion v){ return supported.getOrDefault(logicalType, new TreeSet<>()).contains(v); }
    @Override public String toString(){
        return supported.entrySet().stream()
                .map(e -> e.getKey()+":"+e.getValue()).collect(Collectors.joining(","));
    }
}
