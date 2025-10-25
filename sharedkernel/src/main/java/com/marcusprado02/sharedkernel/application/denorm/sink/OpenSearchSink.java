package com.marcusprado02.sharedkernel.application.denorm.sink;

import com.marcusprado02.sharedkernel.application.denorm.DenormSink;

import java.util.Map;

import org.opensearch.client.opensearch.OpenSearchClient;

public class OpenSearchSink implements DenormSink {
    private final OpenSearchClient client;

    public OpenSearchSink(OpenSearchClient c){ this.client = c; }

    @Override public void upsert(String index, String id, Map<String, Object> doc) {
        try { client.index(i -> i.index(index).id(id).document(doc)); }
        catch (Exception e){ throw new RuntimeException(e); }
    }
    @Override public void delete(String index, String id) {
        try { client.delete(d -> d.index(index).id(id)); }
        catch (Exception e){ throw new RuntimeException(e); }
    }
}
