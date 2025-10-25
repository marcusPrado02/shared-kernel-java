package com.marcusprado02.sharedkernel.infrastructure.email.api;

import com.marcusprado02.sharedkernel.infrastructure.email.model.EmailContext;

public interface EmailRouter {
    /** Seleção de provider por tenant/uso/categoria/região/custo. */
    String route(EmailContext ctx);
}