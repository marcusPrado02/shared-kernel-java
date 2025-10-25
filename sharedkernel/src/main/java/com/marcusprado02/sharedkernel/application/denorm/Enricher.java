package com.marcusprado02.sharedkernel.application.denorm;

import java.util.Map;
import java.util.function.Function;

public interface Enricher extends Function<Map<String,Object>, Map<String,Object>> {
    @Override
    Map<String,Object> apply(Map<String,Object> input);

    
}
