package com.marcusprado02.sharedkernel.infrastructure.secrets;

import java.time.Duration;
import java.util.Set;

public record ReadOptions(
        boolean decodeBase64,       // para backends que retornam binário/base64
        boolean failIfMissing,      // true => lança exceção se não existir
        boolean requestFresh,       // ignora cache (força backend)
        Duration  maxStaleness,     // se cacheado, aceita staleness até este limite
        Set<String> fieldsWhitelist // para JSON: restringe campos
){
    public static ReadOptions defaults(){ return new ReadOptions(false, true, false, Duration.ofSeconds(0), Set.of()); }
    public ReadOptions withFresh(){ return new ReadOptions(decodeBase64, failIfMissing, true, maxStaleness, fieldsWhitelist); }
}