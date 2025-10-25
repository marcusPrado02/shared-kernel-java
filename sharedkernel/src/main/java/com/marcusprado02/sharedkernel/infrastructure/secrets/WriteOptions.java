package com.marcusprado02.sharedkernel.infrastructure.secrets;


import java.time.Duration;
import java.util.*;

public record WriteOptions(String ifMatchEtag, Duration ttl, Map<String,String> metadata, boolean createIfMissing) {
    public static WriteOptions upsert(){ return new WriteOptions(null, null, Map.of(), true); }
}


