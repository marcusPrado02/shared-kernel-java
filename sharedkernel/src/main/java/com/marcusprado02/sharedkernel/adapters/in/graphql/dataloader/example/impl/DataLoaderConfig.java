package com.marcusprado02.sharedkernel.adapters.in.graphql.dataloader.example.impl;

import org.dataloader.BatchLoader;
import org.dataloader.DataLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import  reactor.core.publisher.Flux;
import com.marcusprado02.sharedkernel.contracts.graphql.example.impl.UserAppPort;

@Configuration
public class DataLoaderConfig {
    public static final String USER_BY_ID = "USER_BY_ID";

    @Bean
    public void registerUserLoader(org.springframework.graphql.execution.BatchLoaderRegistry registry, UserBatchService svc) {
        registry.forName(USER_BY_ID)
            .registerBatchLoader((ids, env) ->
                Flux.fromIterable(
                    svc.batchFindByIds(
                        ids.stream()
                            .map(id -> (UUID) id)
                            .toList()
                    )
                )
            );
    }

    public interface UserBatchService {
        List<Optional<UserAppPort.UserDTO>> batchFindByIds(List<UUID> ids);
    }
}

