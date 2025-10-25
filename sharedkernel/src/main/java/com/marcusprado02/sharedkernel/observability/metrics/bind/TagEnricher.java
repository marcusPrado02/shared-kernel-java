package com.marcusprado02.sharedkernel.observability.metrics.bind;

import java.util.Map;

public interface TagEnricher {
    Map<String,String> enrich(Map<String,String> base);
    static TagEnricher noop(){ return base -> base; }
}