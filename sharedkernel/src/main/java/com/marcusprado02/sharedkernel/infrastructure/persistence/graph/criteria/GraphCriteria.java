package com.marcusprado02.sharedkernel.infrastructure.persistence.graph.criteria;

import java.util.*;
public record GraphCriteria(
  String label,                        // label/label set do vértice âncora
  List<NodeFilter> nodeFilters,        // filtros por propriedade
  Optional<Pattern> pattern,           // opcional: encadeamento de relações
  Optional<Sort> sort
){
  public static GraphCriteria of(String label, NodeFilter... fs) {
    return new GraphCriteria(label, List.of(fs), Optional.empty(), Optional.empty());
  }
  public GraphCriteria withPattern(Pattern p){ return new GraphCriteria(label, nodeFilters, Optional.of(p), sort); }
  public GraphCriteria sortBy(Sort s){ return new GraphCriteria(label, nodeFilters, pattern, Optional.of(s)); }
}
