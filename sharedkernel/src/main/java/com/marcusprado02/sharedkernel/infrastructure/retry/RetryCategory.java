package com.marcusprado02.sharedkernel.infrastructure.retry;


public enum RetryCategory {
    SUCCESS,             // 2xx
    RETRYABLE,           // falha transitória segura p/ retry
    THROTTLED,           // 429/limite -> respeitar Retry-After
    UNRETRYABLE,         // 4xx permanentes (ex.: 400, 401, 403, 422)
    FATAL                // bugs locais (codec), não insista
}
