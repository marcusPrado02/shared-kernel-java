package com.marcusprado02.sharedkernel.crosscutting.converters.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcusprado02.sharedkernel.crosscutting.converters.core.BidiConverter;
import com.marcusprado02.sharedkernel.crosscutting.converters.core.ConversionException;
import com.marcusprado02.sharedkernel.crosscutting.converters.core.Converter;

public final class JsonNodePojo<T> implements BidiConverter<JsonNode, T> {
    private final Class<T> type;
    private final ObjectMapper M = new ObjectMapper()
        .findAndRegisterModules();
    public JsonNodePojo(Class<T> type) { this.type = type; }

    @Override public T convert(JsonNode n) {
        try { return M.treeToValue(n, type); }
        catch (Exception e) { throw new ConversionException("JSON→POJO error", e); }
    }
    @Override public Converter<T, JsonNode> inverse() {
        return pojo -> M.valueToTree(pojo);
    }
}

