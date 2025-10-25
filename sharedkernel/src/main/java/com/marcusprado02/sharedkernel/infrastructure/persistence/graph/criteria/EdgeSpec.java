package com.marcusprado02.sharedkernel.infrastructure.persistence.graph.criteria;

import java.util.Map;

public record EdgeSpec(String label, Direction direction, Map<String,Object> props,
                       int minDepth, int maxDepth) {
  public static EdgeSpec of(String label, Direction dir){ return new EdgeSpec(label, dir, Map.of(), 1, 1); }
}
