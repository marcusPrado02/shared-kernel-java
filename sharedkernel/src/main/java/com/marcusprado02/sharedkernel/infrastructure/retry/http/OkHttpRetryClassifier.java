package com.marcusprado02.sharedkernel.infrastructure.retry.http;

import okhttp3.Response;

import java.io.IOException;
import java.time.Duration;

import com.marcusprado02.sharedkernel.infrastructure.retry.*;

public class OkHttpRetryClassifier implements RetryClassifier<Response, IOException> {
    @Override public RetryCategory classify(Response resp, IOException error) {
        if (error != null) {
            var msg = error.getMessage()==null? "" : error.getMessage().toLowerCase();
            if (msg.contains("timeout") || msg.contains("connection reset") || msg.contains("refused"))
                return RetryCategory.RETRYABLE;
            return RetryCategory.RETRYABLE; // falhas de rede em geral
        }
        if (resp == null) return RetryCategory.FATAL;

        int code = resp.code();
        if (code >= 200 && code < 300) return RetryCategory.SUCCESS;
        if (code == 429 || code == 503) return RetryCategory.THROTTLED;
        if (code >= 500 && code < 600) return RetryCategory.RETRYABLE;
        if (code == 408) return RetryCategory.RETRYABLE;
        if (code == 409) return RetryCategory.RETRYABLE; // ex.: race cond. esporádica
        return switch (code) {
            case 400, 401, 403, 404, 405, 422 -> RetryCategory.UNRETRYABLE;
            default -> RetryCategory.UNRETRYABLE;
        };
    }

    @Override public long retryAfterMillis(Response resp) {
        if (resp == null) return -1;
        String h = resp.header("Retry-After");
        if (h == null) h = resp.header("X-RateLimit-Reset-After");
        if (h == null) return -1;
        try {
            // "120" (segundos) ou HTTP-date; aqui tratamos número
            long sec = Long.parseLong(h.trim());
            return Duration.ofSeconds(sec).toMillis();
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}