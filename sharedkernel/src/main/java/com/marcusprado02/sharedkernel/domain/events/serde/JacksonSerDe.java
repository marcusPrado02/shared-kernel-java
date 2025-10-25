package com.marcusprado02.sharedkernel.domain.events.serde;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class JacksonSerDe<T> implements EventSerDe<T> {
    private final ObjectMapper mapper = new ObjectMapper();
    @Override public byte[] serialize(T event){ try { return mapper.writeValueAsBytes(event); } catch (Exception e){ throw new RuntimeException(e); } }
    @Override public T deserialize(byte[] b, Class<T> type){ try { return mapper.readValue(b, type); } catch (Exception e){ throw new RuntimeException(e); } }
    @Override public String contentType(){ return "application/json"; }
}
