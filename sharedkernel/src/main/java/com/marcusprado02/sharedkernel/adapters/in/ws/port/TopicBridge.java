package com.marcusprado02.sharedkernel.adapters.in.ws.port;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Map;

import com.marcusprado02.sharedkernel.contracts.ws.WsEvent;

public interface TopicBridge {
    /** Eventos emitidos pelo domínio para um canal lógico. */
    Flux<WsEvent<?>> subscribe(String channel,
                                                       String tenant, String subject,
                                                       Map<String,Object> args);

    /** Publicação vinda do cliente (opcional): valida, autoriza e aciona domínio. */
    Mono<Void> publish(String channel, String tenant, String subject,
                       String op, Map<String,Object> meta, Object payload);
}
