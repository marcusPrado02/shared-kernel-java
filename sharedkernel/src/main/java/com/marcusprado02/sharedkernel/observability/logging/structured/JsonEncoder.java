package com.marcusprado02.sharedkernel.observability.logging.structured;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public final class JsonEncoder implements Encoder<byte[]> {
    private final ObjectMapper mapper = new ObjectMapper()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    @Override public byte[] encode(StructuredRecord r) throws Exception {
        return mapper.writeValueAsBytes(r);
    }
}