package com.marcusprado02.sharedkernel.crosscutting.exception.adapter.rest;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import com.marcusprado02.sharedkernel.crosscutting.exception.core.*;

public final class ProblemJson {
    public static Map<String,Object> of(MappedError e, String typeBase, String traceId) {
        var type = typeBase + "/" + e.errorCode().toLowerCase(Locale.ROOT).replace('_','-');
        var map = new LinkedHashMap<String,Object>();
        map.put("type", type);
        map.put("title", e.title());
        map.put("status", e.status());
        map.put("detail", e.detail());
        if (e.instance()!=null) map.put("instance", e.instance());
        map.put("error_code", e.errorCode());
        map.put("trace_id", traceId);
        if (e.extra()!=null && !e.extra().isEmpty()) map.putAll(e.extra());
        return map;
    }
}

