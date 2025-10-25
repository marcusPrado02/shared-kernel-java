package com.marcusprado02.sharedkernel.infrastructure.persistence.graph.mapping;

import java.util.Map;

public interface GraphEntityMapper<E> {
  String vertexLabel();
  String idProperty();                 // "id"
  String versionProperty();            // "version"
  Map<String,Object> toProperties(E e);// inclui tenantId, domain props
  E fromProperties(Map<String,Object> props);
}

