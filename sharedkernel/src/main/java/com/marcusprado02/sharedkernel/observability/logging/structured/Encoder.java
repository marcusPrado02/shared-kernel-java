package com.marcusprado02.sharedkernel.observability.logging.structured;

public interface Encoder<T> {
    /** Transforma um StructuredRecord já enriquecido em payload final. */
    T encode(StructuredRecord record) throws Exception;
    default String contentType(){ return "application/json"; }
}
