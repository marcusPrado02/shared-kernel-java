package com.marcusprado02.sharedkernel.adapters.in.ws.config;

import org.springframework.context.annotation.*;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;

import com.marcusprado02.sharedkernel.adapters.in.ws.gateway.WebSocketGatewayHandler;

import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.WebsocketServerSpec;

import java.time.Duration;
import java.util.Map;

@Configuration
public class WebSocketConfig {

    @Bean
    public HandlerMapping wsMapping(WebSocketGatewayHandler handler) {
        var map = Map.<String, WebSocketHandler>of("/ws", handler);
        var hm = new SimpleUrlHandlerMapping();
        hm.setUrlMap(map);
        hm.setOrder(-1);
        return hm;
    }

    @Bean public WebSocketHandlerAdapter wsHandlerAdapter() { return new WebSocketHandlerAdapter(); }

    /** Opcional: servir HTTP com Reactor Netty “tunado” (compressão/WS-spec). */
    @Bean
    public HttpServer httpServer(WebHttpHandlerBuilder builder) {
        var httpHandler = builder.build();
        var adapter = new ReactorHttpHandlerAdapter(httpHandler);
        return HttpServer.create()
            .compress(true)
            .wiretap(false)
            .idleTimeout(Duration.ofMinutes(5))
            .handle(adapter);

    }
}

