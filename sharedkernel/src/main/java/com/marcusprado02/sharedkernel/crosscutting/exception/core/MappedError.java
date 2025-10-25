package com.marcusprado02.sharedkernel.crosscutting.exception.core;

import java.util.LinkedHashMap;
import java.util.Map;

public record MappedError(
    int status,                 // HTTP status ou mapeável p/ gRPC
    String errorCode,           // estável: ex. "USER_NOT_FOUND"
    String title,               // curto e i18n-key friendly
    String detail,              // mensagem segura ao cliente
    String instance,            // URI do recurso/rota, se houver
    Map<String, Object> extra,  // campos adicionais (ex.: fieldErrors)
    Map<String, String> headers // HTTP headers opcionais
) {
    public static Builder builder() { return new Builder(); }
    public static final class Builder {
        private int status = 500;
        private String code = "INTERNAL_ERROR";
        private String title = "Internal Error";
        private String detail = "An unexpected error occurred.";
        private String instance = null;
        private Map<String,Object> extra = new LinkedHashMap<>();
        private Map<String,String> headers = new LinkedHashMap<>();
        public Builder status(int s){this.status=s;return this;}
        public Builder code(String c){this.code=c;return this;}
        public Builder title(String t){this.title=t;return this;}
        public Builder detail(String d){this.detail=d;return this;}
        public Builder instance(String i){this.instance=i;return this;}
        public Builder extra(String k,Object v){this.extra.put(k,v);return this;}
        public Builder header(String k,String v){this.headers.put(k,v);return this;}
        public MappedError build(){return new MappedError(status, code, title, detail, instance, extra, headers);}
    }
}