package com.marcusprado02.sharedkernel.crosscutting.exception.adapter.jaxrs;

import com.marcusprado02.sharedkernel.crosscutting.exception.adapter.rest.ProblemJson;
import com.marcusprado02.sharedkernel.crosscutting.exception.core.ExceptionMapperRegistry;
import com.marcusprado02.sharedkernel.crosscutting.exception.core.MappedError;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.container.ContainerRequestContext;

@Provider
@Priority(Priorities.USER)
public class JaxrsExceptionMapper implements jakarta.ws.rs.ext.ExceptionMapper<Throwable> {
    @Inject ExceptionMapperRegistry<ContainerRequestContext> registry;

    @Override
    public Response toResponse(Throwable ex) {
        // Se preferir, injete @Context ContainerRequestContext reqCtx; aqui passamos null
        String traceId = io.opentelemetry.api.trace.Span.current().getSpanContext().getTraceId();

        MappedError mapped = registry.map(ex, null); // tipagem explícita evita inferência como Object
        var entity = ProblemJson.of(mapped, "https://errors.acme.com", traceId);

        var rb = Response.status(mapped.status())
                         .type("application/problem+json")
                         .entity(entity);
        mapped.headers().forEach(rb::header);
        return rb.build();
    }
}
