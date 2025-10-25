package com.marcusprado02.sharedkernel.infrastructure.cache;


import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import reactor.core.publisher.Mono;

public record CacheOptions(
    Duration ttl,              // Time-To-Live (expiração fixa)
    Optional<Duration> tti,    // Time-To-Idle (renova no acesso)
    boolean cacheNulls,        // salvar nulls (com TTL curto) para evitar dogpiles
    boolean compress,          // compressão (GZIP/LZ4/Zstd)
    boolean versioned,         // inclui versão do objeto/chave
    boolean weakConsistency,   // permite staleness controlado (stale-while-revalidate)
    boolean singleFlight,      // colapsa concorrência p/ mesma chave (stampede)
    double jitterFactor        // 0..1 p/ aleatorizar TTL
) {
  public static CacheOptions defaults() {
    return new CacheOptions(Duration.ofMinutes(5), Optional.empty(), true, true, true, true, true, 0.1);
  }
}
