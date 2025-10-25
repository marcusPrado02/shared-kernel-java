package com.marcusprado02.sharedkernel.infrastructure.sms.api;

import com.marcusprado02.sharedkernel.infrastructure.sms.model.SmsContext;

public interface SmsRouter {
    /** Escolha de provider por tenant/país/tipo de mensagem/custo/latência. */
    String route(SmsContext ctx);
}
