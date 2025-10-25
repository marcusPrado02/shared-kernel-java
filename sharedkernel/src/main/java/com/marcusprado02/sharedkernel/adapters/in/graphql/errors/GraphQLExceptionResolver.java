package com.marcusprado02.sharedkernel.adapters.in.graphql.errors;

import graphql.*;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.stereotype.Component;

@Component
public class GraphQLExceptionResolver extends DataFetcherExceptionResolverAdapter {
    protected graphql.GraphQLError resolveToSingleError(Throwable ex, graphql.GraphQLError error) {
        // Produza extensões com códigos e correlação
        return GraphqlErrorBuilder.newError()
                .message(ex.getMessage() != null ? ex.getMessage() : "Internal error")
                .errorType(ErrorType.DataFetchingException)
                .extensions(java.util.Map.of(
                        "code", "INTERNAL",
                        "correlationId", org.slf4j.MDC.get("correlationId")))
                .path(error.getPath())
                .location(error.getLocations() != null && !error.getLocations().isEmpty()
                        ? error.getLocations().get(0) : null)
                .build();
    }
}
