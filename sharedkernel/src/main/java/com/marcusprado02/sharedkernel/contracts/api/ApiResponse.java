package com.marcusprado02.sharedkernel.contracts.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("rawtypes" + "unchecked")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        T data,
        Map<String, Object> meta,
        Map<String, String> links,
        String warning,
        int status,
        Map<String, String> headers,
        byte[] body,
        Instant finishedAt) {

        public static Builder builder() { return new Builder(); }
        public static final class Builder {
        private int status = 200;
        private final Map<String, String> headers = new LinkedHashMap<>();
        private byte[] body = new byte[0];
        private Instant finishedAt;
        public Builder status(int s){ 
                this.status = s; 
                return this; 
        }
        public Builder header(String k, String v){ 
                headers.put(k, v); 
                return this; 
        }
        public Builder body(byte[] b){ 
                this.body = b; 
                return this; 
        }
        public Builder finishedNow(){ 
                this.finishedAt = Instant.now(); 
                return this; 
        }
        
        public ApiResponse build(){ 
                return new ApiResponse(null, null, null, null, status, headers, body, finishedAt);
        }
        public static <T> ApiResponse<T> success(T data, Map<String,Object> meta, Map<String,String> links) {
        return new ApiResponse<>(data, meta, links, null, 200, Map.of(), null, null);
        }
        public static <T> ApiResponse<T> created(T data, Map<String,Object> meta, Map<String,String> links) {
                return new ApiResponse<>(data, meta, links, null, 201, Map.of(), null, null);
        }
    }
}