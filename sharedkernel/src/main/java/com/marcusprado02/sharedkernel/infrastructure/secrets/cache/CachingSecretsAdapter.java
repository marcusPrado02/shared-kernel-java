package com.marcusprado02.sharedkernel.infrastructure.secrets.cache;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import com.marcusprado02.sharedkernel.infrastructure.secrets.ListResult;
import com.marcusprado02.sharedkernel.infrastructure.secrets.CryptoCapabilities;
import com.marcusprado02.sharedkernel.infrastructure.secrets.PutSecretRequest;
import com.marcusprado02.sharedkernel.infrastructure.secrets.ReadOptions;
import com.marcusprado02.sharedkernel.infrastructure.secrets.RotationPolicy;
import com.marcusprado02.sharedkernel.infrastructure.secrets.Secret;
import com.marcusprado02.sharedkernel.infrastructure.secrets.SecretId;
import com.marcusprado02.sharedkernel.infrastructure.secrets.SecretMetadata;
import com.marcusprado02.sharedkernel.infrastructure.secrets.SecretRef;
import com.marcusprado02.sharedkernel.infrastructure.secrets.SecretType;
import com.marcusprado02.sharedkernel.infrastructure.secrets.SecretValue;
import com.marcusprado02.sharedkernel.infrastructure.secrets.SecretsAdapter;
import com.marcusprado02.sharedkernel.infrastructure.secrets.WriteOptions;

public class CachingSecretsAdapter implements SecretsAdapter {

    private final SecretsAdapter delegate;
    private final long ttlSeconds; // TTL do cache
    private final Map<String, CacheEntry<?>> cache = new ConcurrentHashMap<>();

    public CachingSecretsAdapter(SecretsAdapter delegate, long ttlSeconds){
        this.delegate = delegate; this.ttlSeconds = ttlSeconds;
    }

    private record CacheEntry<T>(Secret<T> secret, Instant expiresAt){}

    private static String key(SecretId id, SecretType t){
        return id.namespace()+"|"+id.name()+"|"+Optional.ofNullable(id.version()).orElse("latest")+
               "|"+Optional.ofNullable(id.stage()).orElse("-")+"|"+t;
    }

    @SuppressWarnings("unchecked")
    private <T> Secret<T> getOrLoad(String k, Supplier<Secret<T>> loader){
        var now = Instant.now();
        var e = (CacheEntry<T>) cache.get(k);
        if (e != null && now.isBefore(e.expiresAt())) return e.secret;
        var fresh = loader.get();
        cache.put(k, new CacheEntry<>(fresh, now.plusSeconds(ttlSeconds)));
        return fresh;
    }

    private static SecretId toId(SecretRef ref){
        String ns = Optional.ofNullable(ref.environment()).orElse("/");
        return SecretId.of(ns, ref.path());
    }

    private void invalidate(SecretId id){
        String prefix = id.namespace()+"|"+id.name()+"|";
        cache.keySet().removeIf(k -> k.startsWith(prefix));
    }
    private void invalidate(SecretRef ref){ invalidate(toId(ref)); }

    // -------- Leitura com cache (API tipada por SecretId) --------
    @Override public Secret<String> getString(SecretId id, ReadOptions o){
        if (o.requestFresh()) return delegate.getString(id, o);
        return getOrLoad(key(id, SecretType.STRING), () -> delegate.getString(id, o));
    }
    @Override public Secret<Map<String, Object>> getJson(SecretId id, ReadOptions o){
        if (o.requestFresh()) return delegate.getJson(id, o);
        return getOrLoad(key(id, SecretType.JSON), () -> delegate.getJson(id, o));
    }
    @Override public Secret<byte[]> getBinary(SecretId id, ReadOptions o){
        if (o.requestFresh()) return delegate.getBinary(id, o);
        return getOrLoad(key(id, SecretType.BINARY), () -> delegate.getBinary(id, o));
    }

    // -------- Mutations invalidam cache --------
    @Override public Secret<String> put(PutSecretRequest req){
        var r = delegate.put(req); invalidate(req.id()); return r;
    }
    @Override public void delete(SecretId id, boolean all){ delegate.delete(id, all); invalidate(id); }
    @Override public void promoteStage(SecretId id, String from, String to){ delegate.promoteStage(id, from, to); invalidate(id); }
    @Override public Secret<String> rotate(SecretId id, RotationPolicy p, java.util.function.Supplier<Object> g){
        var r = delegate.rotate(id, p, g); invalidate(id); return r;
    }

    // -------- Listing / versions / async delegados --------
    @Override public List<Secret<String>> listVersions(SecretId id, int pageSize, String cursor){ return delegate.listVersions(id, pageSize, cursor); }
    @Override public ListResult list(String namespace, String prefix, int pageSize, String cursor){ return delegate.list(namespace, prefix, pageSize, cursor); }
    @Override public CompletableFuture<Secret<String>> getStringAsync(SecretId id, ReadOptions o){ return delegate.getStringAsync(id, o); }
    @Override public CompletableFuture<Secret<Map<String, Object>>> getJsonAsync(SecretId id, ReadOptions o){ return delegate.getJsonAsync(id, o); }
    @Override public CompletableFuture<Secret<byte[]>> getBinaryAsync(SecretId id, ReadOptions o){ return delegate.getBinaryAsync(id, o); }
    @Override public Optional<CryptoCapabilities> crypto(){ return delegate.crypto(); }
    @Override public String backendName(){ return "Caching("+delegate.backendName()+")"; }

    // -------- API baseada em SecretRef (exigida pela interface) --------
    @Override public SecretValue getSecret(SecretRef ref, ReadOptions opts){
        // Sem cache específico para SecretValue (tipo/content-type variam); delega direto
        return delegate.getSecret(ref, opts);
    }

    @Override public void putSecret(SecretRef ref, SecretValue value, WriteOptions opts){
        delegate.putSecret(ref, value, opts);
        invalidate(ref);
    }

    @Override public CompletableFuture<SecretValue> getSecretAsync(SecretRef ref, ReadOptions opts){
        return delegate.getSecretAsync(ref, opts);
    }

    @Override public CompletableFuture<Void> putSecretAsync(SecretRef ref, SecretValue value, WriteOptions opts){
        return delegate.putSecretAsync(ref, value, opts)
                       .whenComplete((v, ex) -> { if (ex == null) invalidate(ref); });
    }

    @Override public SecretMetadata stat(SecretRef ref){ return delegate.stat(ref); }
    @Override public void deleteSecret(SecretRef ref, boolean deleteVersions){ delegate.deleteSecret(ref, deleteVersions); invalidate(ref); }
    @Override public void enableVersion(SecretRef ref, String versionId, boolean enabled){ delegate.enableVersion(ref, versionId, enabled); invalidate(ref); }
    @Override public String rotate(SecretRef ref, RotationPolicy policy){ var v = delegate.rotate(ref, policy); invalidate(ref); return v; }
    @Override public List<String> list(SecretRef prefix){ return delegate.list(prefix); }
}
