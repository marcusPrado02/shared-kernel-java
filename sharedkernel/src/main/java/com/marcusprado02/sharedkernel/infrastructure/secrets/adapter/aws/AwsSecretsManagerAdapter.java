package com.marcusprado02.sharedkernel.infrastructure.secrets.adapter.aws;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.Tracer;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import software.amazon.awssdk.core.SdkBytes;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.marcusprado02.sharedkernel.infrastructure.secrets.BaseSecretsAdapter;
import com.marcusprado02.sharedkernel.infrastructure.secrets.ListResult;
import com.marcusprado02.sharedkernel.infrastructure.secrets.PutSecretRequest;
import com.marcusprado02.sharedkernel.infrastructure.secrets.ReadOptions;
import com.marcusprado02.sharedkernel.infrastructure.secrets.RotationPolicy;
import com.marcusprado02.sharedkernel.infrastructure.secrets.Secret;
import com.marcusprado02.sharedkernel.infrastructure.secrets.SecretId;
import com.marcusprado02.sharedkernel.infrastructure.secrets.SecretMetadata;
import com.marcusprado02.sharedkernel.infrastructure.secrets.SecretRef;
import com.marcusprado02.sharedkernel.infrastructure.secrets.SecretType;
import com.marcusprado02.sharedkernel.infrastructure.secrets.SecretValue;
import com.marcusprado02.sharedkernel.infrastructure.secrets.SecretVersion;
import com.marcusprado02.sharedkernel.infrastructure.secrets.WriteOptions;
import com.marcusprado02.sharedkernel.infrastructure.secrets.SecretsCache;

public class AwsSecretsManagerAdapter extends BaseSecretsAdapter {

    private final SecretsManagerClient client;

    public AwsSecretsManagerAdapter(Tracer tracer, MeterRegistry meter, Retry retry, CircuitBreaker cb,
                                    SecretsCache cache, SecretsManagerClient client) {
        super(tracer, meter, retry, cb, cache);
        this.client = client;
    }

    @Override
    protected SecretValue doGet(SecretRef ref, ReadOptions opts) {
        var name = ref.fqn();
        GetSecretValueRequest.Builder b = GetSecretValueRequest.builder().secretId(name);
        GetSecretValueResponse r = client.getSecretValue(b.build());
        if (r.secretBinary()!=null) {
            return new SecretValue(r.secretBinary().asByteArray(), "application/octet-stream");
        }
        return new SecretValue(r.secretString().toCharArray(), "text/plain");
    }

    @Override
    protected void doPut(SecretRef ref, SecretValue value, WriteOptions opts) {
        var name = ref.fqn();
        // Se não existir, createSecret; senão putSecretValue
        try {
            client.describeSecret(DescribeSecretRequest.builder().secretId(name).build());
            PutSecretValueRequest.Builder b = PutSecretValueRequest.builder().secretId(name);
            if (value.text().isPresent()) b.secretString(new String(value.text().get()));
            else if (value.bytes().isPresent()) b.secretBinary(SdkBytes.fromByteArray(value.bytes().get()));
            client.putSecretValue(b.build());
        } catch (ResourceNotFoundException notFound) {
            CreateSecretRequest.Builder c = CreateSecretRequest.builder().name(name);
            if (value.text().isPresent()) c.secretString(new String(value.text().get()));
            else if (value.bytes().isPresent()) c.secretBinary(SdkBytes.fromByteArray(value.bytes().get()));
            client.createSecret(c.build());
        }
    }

    @Override
    protected void doDelete(SecretRef ref, boolean deleteVersions) {
        client.deleteSecret(DeleteSecretRequest.builder()
                .secretId(ref.fqn())
                .forceDeleteWithoutRecovery(deleteVersions) // se true: destrói imediatamente
                .build());
    }

    @Override
    protected SecretMetadata doStat(SecretRef ref) {
        var d = client.describeSecret(DescribeSecretRequest.builder().secretId(ref.fqn()).build());
        var versions = d.versionIdsToStages().entrySet().stream()
                .map(e -> new SecretVersion(e.getKey(), d.createdDate(), !e.getValue().contains("AWSPREVIOUS"), null))
                .toList();
        return new SecretMetadata(ref, "AWS", null, versions, d.tags()==null? Map.of(): d.tags().stream().collect(
                java.util.stream.Collectors.toMap(Tag::key, Tag::value)));
    }

    @Override
    protected void doEnableVersion(SecretRef ref, String versionId, boolean enabled) {
        // AWS não habilita/disable diretamente; atualiza labels (AWSCURRENT/AWSPREVIOUS)
        // Simplificação: set AWSCURRENT p/ versionId
        client.updateSecretVersionStage(UpdateSecretVersionStageRequest.builder()
                .secretId(ref.fqn())
                .moveToVersionId(versionId)
                .versionStage("AWSCURRENT")
                .build());
    }

    @Override
    protected String doRotate(SecretRef ref, RotationPolicy policy) {
        var r = client.rotateSecret(RotateSecretRequest.builder()
                .secretId(ref.fqn())
                .rotationLambdaARN(policy.rotationLambdaOrWebhook()) // se usar Lambda
                .rotationRules(RotationRulesType.builder()
                        .automaticallyAfterDays(policy.interval().toDays()).build())
                .build());
        return r.versionId();
    }

    @Override
    protected List<String> doList(SecretRef prefix) {
        var out = new ArrayList<String>();
        String next = null;
        do {
            var r = client.listSecrets(ListSecretsRequest.builder().nextToken(next).build());
            r.secretList().forEach(s -> {
                if (prefix==null || s.name().startsWith(prefix.fqn())) out.add(s.name());
            });
            next = r.nextToken();
        } while (next != null);
        return out;
    }

        private String fullName(SecretId id){
        // convenção: namespace/name -> "/namespace/name"
        String base = (id.namespace().startsWith("/") ? id.namespace() : "/"+id.namespace()) + "/" + id.name();
        return base.replaceAll("//+","/");
    }

    @Override
    public Secret<String> getString(SecretId id, ReadOptions opts) {
        return withSpan("getString", id, () -> {
            var b = GetSecretValueRequest.builder().secretId(fullName(id));
            if (id.version() != null) {
                if (id.version().matches("^[a-f0-9-]{36}$")) b.versionId(id.version());
                else b.versionStage(id.version()); // permite "AWSCURRENT"
            } else if (id.stage() != null) b.versionStage(id.stage());

            var r = client.getSecretValue(b.build());
            String payload = r.secretString() != null ? r.secretString() : new String(r.secretBinary().asByteArray(), StandardCharsets.UTF_8);

            return new Secret<>(
                    id, SecretType.STRING, payload, r.versionId(),
                    r.versionStages().isEmpty()? null : r.versionStages().get(0),
                    r.createdDate()==null? null : r.createdDate(),
                    null, // Secrets Manager não expõe expires padrão do valor
                    Map.of(), r.versionId()
            );
        });
    }

    @Override
    public Secret<Map<String, Object>> getJson(SecretId id, ReadOptions opts) {
        var s = getString(id, opts);
        Map<String,Object> map;
        try {
            map = JsonMapper.builder().build()
                    .readValue(s.value(), new com.fasterxml.jackson.core.type.TypeReference<Map<String,Object>>() {});
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse JSON secret for " + id, e);
        }
        if (!opts.fieldsWhitelist().isEmpty()) map.keySet().removeIf(k -> !opts.fieldsWhitelist().contains(k));
        return new Secret<>(s.id(), SecretType.JSON, map, s.versionId(), s.stage(), s.createdAt(), s.expiresAt(), s.metadata(), s.etag());
    }

    @Override
    public Secret<byte[]> getBinary(SecretId id, ReadOptions opts) {
        return withSpan("getBinary", id, () -> {
            var r = client.getSecretValue(GetSecretValueRequest.builder().secretId(fullName(id)).build());
            byte[] data = r.secretBinary() != null ? r.secretBinary().asByteArray() : r.secretString().getBytes(StandardCharsets.UTF_8);
            return new Secret<>(id, SecretType.BINARY, data, r.versionId(), null, r.createdDate(), null, Map.of(), r.versionId());
        });
    }

    @Override
    public Secret<String> put(PutSecretRequest req) {
        return withSpan("put", req.id(), () -> {
            String name = fullName(req.id());
            // createIfMissing
            try {
                client.describeSecret(DescribeSecretRequest.builder().secretId(name).build());
            } catch (ResourceNotFoundException nf) {
                if (req.createIfMissing()) {
                    client.createSecret(CreateSecretRequest.builder()
                            .name(name)
                            .secretString(req.type()==SecretType.BINARY ? null : (String) req.value())
                            .secretBinary(req.type()==SecretType.BINARY ? software.amazon.awssdk.core.SdkBytes.fromByteArray((byte[]) req.value()) : null)
                            .tags(req.tags().entrySet().stream().map(e -> Tag.builder().key(e.getKey()).value(e.getValue()).build()).toList())
                            .build());
                } else {
                    throw nf;
                }
            }

            PutSecretValueResponse r = client.putSecretValue(PutSecretValueRequest.builder()
                    .secretId(name)
                    .secretString(req.type()==SecretType.BINARY ? null : (String) req.value())
                    .secretBinary(req.type()==SecretType.BINARY ? software.amazon.awssdk.core.SdkBytes.fromByteArray((byte[]) req.value()) : null)
                    .versionStages(req.overwriteCurrent() ? List.of("AWSCURRENT") : List.of())
                    .build());

            return new Secret<>(req.id(), req.type(), (String) (req.type()==SecretType.BINARY ? "<binary>" : req.value()),
                    r.versionId(), "AWSCURRENT", Instant.now(), req.expiresAt(), req.tags(), r.versionId());
        });
    }

    @Override public void delete(SecretId id, boolean deleteAllVersions) {
        withSpan("delete", id, () -> {
            client.deleteSecret(DeleteSecretRequest.builder().secretId(fullName(id))
                    .forceDeleteWithoutRecovery(deleteAllVersions).build());
            return null;
        });
    }

    @Override
    public List<Secret<String>> listVersions(SecretId id, int pageSize, String cursor) {
        return withSpan("listVersions", id, () -> {
            var r = client.listSecretVersionIds(ListSecretVersionIdsRequest.builder()
                    .secretId(fullName(id))
                    .maxResults(pageSize)
                    .nextToken(cursor)
                    .build());
            var versions = new ArrayList<Secret<String>>();
            for (SecretVersionsListEntry e : r.versions()) {
                versions.add(new Secret<>(id, SecretType.STRING, "<redacted>", e.versionId(),
                        e.versionStages().isEmpty()? null : e.versionStages().get(0),
                        e.createdDate(), null, Map.of(), e.versionId()));
            }
            return versions;
        });
    }

    @Override
    public void promoteStage(SecretId id, String fromStage, String toStage) {
        withSpan("promoteStage", id, () -> {
            // Em AWS, associamos stage "AWSCURRENT" à versão alvo
            var versions = client.listSecretVersionIds(ListSecretVersionIdsRequest.builder().secretId(fullName(id)).build()).versions();
            SecretVersionsListEntry target = versions.stream().filter(v -> v.versionStages().contains(fromStage)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("stage not found: "+fromStage));
            client.updateSecretVersionStage(UpdateSecretVersionStageRequest.builder()
                    .secretId(fullName(id))
                    .moveToVersionId(target.versionId())
                    .versionStage(toStage)
                    .removeFromVersionId(target.versionId())
                    .build());
            return null;
        });
    }

    @Override
    public Secret<String> rotate(SecretId id, RotationPolicy policy, java.util.function.Supplier<Object> generator) {
        return withSpan("rotate", id, () -> {
            // Geração local e push como nova versão com stage temporário, depois promove a AWSCURRENT e rebalanceia AWSPREVIOUS
            Object newValue = generator.get();
            PutSecretRequest req = PutSecretRequest.of(id).type(newValue instanceof byte[]? SecretType.BINARY:
                    (newValue instanceof Map<?,?>? SecretType.JSON: SecretType.STRING)).value(newValue).build();
            Secret<String> put = put(req);
            promoteStage(id, "AWSCURRENT", "AWSPREVIOUS");
            // associar nova versão a AWSCURRENT (já feito em put overwrite=true)
            return put;
        });
    }

    @Override
    public ListResult list(String namespace, String prefix, int pageSize, String cursor) {
        var r = client.listSecrets(ListSecretsRequest.builder()
                .filters(Filter.builder()
                        .key(FilterNameStringType.NAME)              // <-- aqui é o enum certo
                        .values((namespace.startsWith("/") ? namespace : "/" + namespace) + "/" + prefix)
                        .build())
                .maxResults(pageSize)
                .nextToken(cursor)
                .build());
        var items = r.secretList().stream()
                .map(s -> {
                    String name = s.name();
                    String ns = name.contains("/") ? name.substring(0, name.lastIndexOf('/')) : "/";
                    String nm = name.substring(name.lastIndexOf('/') + 1);
                    return SecretId.of(ns, nm);
                }).toList();
        String next = r.nextToken();
        return new ListResult(items, next, next != null && !next.isEmpty());

    }
}