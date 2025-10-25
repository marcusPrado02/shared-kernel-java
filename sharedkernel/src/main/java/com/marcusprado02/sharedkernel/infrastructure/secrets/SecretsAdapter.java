package com.marcusprado02.sharedkernel.infrastructure.secrets;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public interface SecretsAdapter {

    SecretValue getSecret(SecretRef ref, ReadOptions opts);                    // retorna valor (caller deve fechar)
    SecretMetadata stat(SecretRef ref);                                        // meta + versões
    void putSecret(SecretRef ref, SecretValue value, WriteOptions opts);       // cria/atualiza
    void deleteSecret(SecretRef ref, boolean deleteVersions);                  // soft/hard
    List<String> list(SecretRef prefix);                                       // lista por prefixo
    void enableVersion(SecretRef ref, String versionId, boolean enabled);      // enable/disable version
    String rotate(SecretRef ref, RotationPolicy policy);                       // gatilha rotação e retorna versão nova

    // Assíncrono
    CompletableFuture<SecretValue> getSecretAsync(SecretRef ref, ReadOptions opts);
    CompletableFuture<Void> putSecretAsync(SecretRef ref, SecretValue value, WriteOptions opts);

    default String backendName(){ return getClass().getSimpleName(); }

        // --- Read ---
    Secret<String>  getString(SecretId id, ReadOptions opts);
    Secret<Map<String,Object>> getJson(SecretId id, ReadOptions opts);
    Secret<byte[]>  getBinary(SecretId id, ReadOptions opts);

    // --- Write / Versioning ---
    Secret<String>  put(PutSecretRequest req); // retorna a versão/etag atualizada
    void            delete(SecretId id, boolean deleteAllVersions);
    List<Secret<String>> listVersions(SecretId id, int pageSize, String cursor);
    void            promoteStage(SecretId id, String fromStage, String toStage); // ex.: AWSPREVIOUS->AWSCURRENT

    // --- Rotation ---
    Secret<String>  rotate(SecretId id, RotationPolicy policy, java.util.function.Supplier<Object> generator);

    // --- Listing ---
    ListResult      list(String namespace, String prefix, int pageSize, String cursor);

    // --- Async ---
    CompletableFuture<Secret<String>> getStringAsync(SecretId id, ReadOptions opts);
    CompletableFuture<Secret<Map<String,Object>>> getJsonAsync(SecretId id, ReadOptions opts);
    CompletableFuture<Secret<byte[]>> getBinaryAsync(SecretId id, ReadOptions opts);

    // --- Crypto (opcional) ---
    default Optional<CryptoCapabilities> crypto(){ return Optional.empty(); }

}