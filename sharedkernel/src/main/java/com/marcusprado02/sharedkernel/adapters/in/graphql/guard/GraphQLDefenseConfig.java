package com.marcusprado02.sharedkernel.adapters.in.graphql.guard;

import graphql.analysis.MaxQueryComplexityInstrumentation;
import graphql.analysis.MaxQueryDepthInstrumentation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GraphQLDefenseConfig {
    @Bean MaxQueryComplexityInstrumentation maxComplexity() { return new MaxQueryComplexityInstrumentation(300); } // ajuste
    @Bean MaxQueryDepthInstrumentation maxDepth() { return new MaxQueryDepthInstrumentation(15); } // ajuste
}
