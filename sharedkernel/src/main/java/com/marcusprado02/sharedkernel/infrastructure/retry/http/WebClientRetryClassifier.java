package com.marcusprado02.sharedkernel.infrastructure.retry.http;

import org.springframework.web.reactive.function.client.ClientResponse;

import com.marcusprado02.sharedkernel.infrastructure.retry.RetryCategory;
import com.marcusprado02.sharedkernel.infrastructure.retry.RetryClassifier;

public class WebClientRetryClassifier implements RetryClassifier<ClientResponse, Throwable> {
    @Override public RetryCategory classify(ClientResponse resp, Throwable error) {
        if (error != null) return RetryCategory.RETRYABLE;
        if (resp == null) return RetryCategory.FATAL;
        int code = resp.rawStatusCode();
        if (code >= 200 && code < 300) return RetryCategory.SUCCESS;
        if (code == 429 || code == 503) return RetryCategory.THROTTLED;
        if (code == 408 || (code >= 500 && code < 600)) return RetryCategory.RETRYABLE;
        return switch (code) {
            case 400, 401, 403, 404, 405, 422 -> RetryCategory.UNRETRYABLE;
            default -> RetryCategory.UNRETRYABLE;
        };
    }

    @Override public long retryAfterMillis(ClientResponse resp) {
        if (resp == null) return -1;
        return resp.headers().asHttpHeaders().getFirst("Retry-After") != null
            ? 1000L * Long.parseLong(resp.headers().asHttpHeaders().getFirst("Retry-After"))
            : -1;
    }
}
