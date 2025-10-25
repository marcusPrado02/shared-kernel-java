package com.marcusprado02.sharedkernel.infrastructure.search;


// -------- Operadores e Critérios --------
public enum Operator {
    EQ, NE, GT, GTE, LT, LTE, IN, NIN, BETWEEN, LIKE, MATCH, PREFIX, SUFFIX, EXISTS,
    GEO_DISTANCE, GEO_BOUNDING_BOX, WITHIN_POLYGON,
    // Para JSON/Docs: caminho com dot-notation
}

