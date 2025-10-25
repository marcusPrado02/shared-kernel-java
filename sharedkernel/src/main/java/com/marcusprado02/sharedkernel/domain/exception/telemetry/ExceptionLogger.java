package com.marcusprado02.sharedkernel.domain.exception.telemetry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.marcusprado02.sharedkernel.domain.exception.domain.DomainException;

public final class ExceptionLogger {
    private static final Logger log = LoggerFactory.getLogger(ExceptionLogger.class);
    public void log(DomainException ex){
        log.warn("domain_exception code={} severity={} retryability={} ctx={} params={}",
                ex.codeFqn(), ex.severity(), ex.retryability(), ex.context(), ex.parameters(), ex);
    }
}
