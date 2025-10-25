package com.marcusprado02.sharedkernel.application.denorm;

import java.util.Map;

public interface DenormSink {
    /** Operação idempotente de upsert/replace/delete lógico no destino. */
    void upsert(String collection, String id, Map<String,Object> document);
    void delete(String collection, String id);
}
