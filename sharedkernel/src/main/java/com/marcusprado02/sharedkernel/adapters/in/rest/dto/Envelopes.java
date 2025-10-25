package com.marcusprado02.sharedkernel.adapters.in.rest.dto;


import java.util.List;
import java.util.Map;

public final class Envelopes {

    public static <T extends ResponseDTO> ApiResponseEnvelope<T> one(T data, String requestId, String correlationId, String etag) {
        var meta = Meta.of(requestId, correlationId, data.contractVersion(), etag, Map.of());
        return new ApiResponseEnvelope<>(data, meta, new Links(null,null,null,null,null));
    }

    public static <T extends ResponseDTO> ApiListEnvelope<T> listOffset(
            List<T> data, String requestId, String correlationId, String self, String next,
            int offset, int limit, long total, String etag) {
        var meta = Meta.of(requestId, correlationId, "v1", etag, Map.of("count", data.size()));
        var links = new Links(self,null,null,next,null);
        return new ApiListEnvelope<>(data, meta, links, new PageOffset(offset, limit, total), null);
    }

    public static <T extends ResponseDTO> ApiListEnvelope<T> listCursor(
            List<T> data, String requestId, String correlationId, String self, String next,
            String after, Integer limit, boolean hasMore, Long totalApprox, String etag) {
        var meta = Meta.of(requestId, correlationId, "v1", etag, Map.of("count", data.size()));
        var links = new Links(self,null,null,next,null);
        return new ApiListEnvelope<>(data, meta, links, null, new PageCursor(null, after, limit, hasMore, totalApprox));
    }
}
