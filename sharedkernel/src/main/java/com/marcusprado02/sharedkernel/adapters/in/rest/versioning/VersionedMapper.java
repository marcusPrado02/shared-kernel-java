package com.marcusprado02.sharedkernel.adapters.in.rest.versioning;

/** Strategia per-resource: despacha para o "mapper" correto. */
public interface VersionedMapper<IN, OUT> {
    OUT v1_0(IN in);
    default OUT v1_1(IN in){ return v1_0(in); } // BC
    OUT v2_0(IN in);
}
