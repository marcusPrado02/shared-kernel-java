package com.marcusprado02.sharedkernel.events.spi;

public interface JsonSerializer {
    String toJson(Object o);
    <T> T fromJson(String json, Class<T> type);
}
