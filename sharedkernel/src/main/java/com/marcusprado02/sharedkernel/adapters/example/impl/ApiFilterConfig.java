package com.marcusprado02.sharedkernel.adapters.example.impl;

import java.util.List;

import org.springframework.context.annotation.Bean;

import com.marcusprado02.sharedkernel.adapters.in.rest.filters.AccessLogFilter;
import com.marcusprado02.sharedkernel.adapters.in.rest.filters.CorrelationFilter;
import com.marcusprado02.sharedkernel.contracts.api.ApiResponse;
import com.marcusprado02.sharedkernel.contracts.api.EndpointHandler;
import com.marcusprado02.sharedkernel.contracts.api.FilterChainBuilder;
import com.marcusprado02.sharedkernel.contracts.api.FilterDef;

// @Configuration
public class ApiFilterConfig {
    @Bean
    public ServletApiFilterBridge apiBridge(List<FilterDef> defs) {
        // Exemplo de EndpointHandler que delega ao DispatcherServlet via lambda simples
        EndpointHandler terminal = ex -> {
            // Aqui normalmente você deixaria o Spring resolver o controller.
            // Se preferir, crie um HandlerAdapter que chama o DispatcherServlet.
            return ex.response().orElseGet(() -> ApiResponse.builder().status(404).finishedNow().build());
        };

        var builder = new FilterChainBuilder()
                .add(CorrelationFilter.def(10))
                .add(AccessLogFilter.def(20));
        defs.forEach(builder::add); // appenda filtros de módulos
        var root = builder.build(terminal);

        return new ServletApiFilterBridge(root, terminal);
    }
}
