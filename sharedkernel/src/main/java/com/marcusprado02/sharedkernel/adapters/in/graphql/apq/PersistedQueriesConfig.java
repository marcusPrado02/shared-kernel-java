package com.marcusprado02.sharedkernel.adapters.in.graphql.apq;


import graphql.execution.preparsed.PreparsedDocumentProvider;
import graphql.execution.preparsed.persisted.ApolloPersistedQuerySupport;
import graphql.execution.preparsed.persisted.InMemoryPersistedQueryCache;
import org.springframework.boot.autoconfigure.graphql.GraphQlSourceBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PersistedQueriesConfig {

    @Bean
    GraphQlSourceBuilderCustomizer apqCustomizer() {
        return builder -> builder.configureGraphQl(graphQLBuilder -> {
            // Cache em memória para APQ; troque por um cache próprio se quiser (Redis, Caffeine etc.)
            PreparsedDocumentProvider provider =
                new ApolloPersistedQuerySupport(
                    InMemoryPersistedQueryCache.newInMemoryPersistedQueryCache().build()
                );
            graphQLBuilder.preparsedDocumentProvider(provider);
        });
    }
}