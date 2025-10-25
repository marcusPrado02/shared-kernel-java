package com.marcusprado02.sharedkernel.infrastructure.payments.api;

import com.marcusprado02.sharedkernel.infrastructure.payments.model.PaymentContext;

public interface PaymentRouter {
    /** Define como escolher o provider com base no contexto (tenant, país, meio, score de risco). */
    String route(PaymentContext ctx);
}