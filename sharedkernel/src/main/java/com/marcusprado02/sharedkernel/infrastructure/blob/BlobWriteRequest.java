package com.marcusprado02.sharedkernel.infrastructure.blob;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record BlobWriteRequest(
        BlobId id,
        InputStream data,
        long contentLength,            // -1 se desconhecido; backends podem exigir
        String contentType,
        StorageClass storageClass,
        AccessTier accessTier,
        CannedAcl acl,
        UserMetadata metadata,
        Tags tags,
        Encryption encryption,
        Retention retention,
        String idempotencyKey,         // para deduplicar tentativas
        boolean overwriteIfExists,     // true => substitui; false => falha se existir
        boolean computeChecksum,       // calcula e envia checksum se suportado
        ContentHash expectedHash       // valida integridade se fornecido
) {
    public static Builder to(BlobId id){ return new Builder(id); }
    public static final class Builder {
        private final BlobId id;
        private InputStream data;
        private long contentLength = -1;
        private String contentType = "application/octet-stream";
        private StorageClass storageClass = StorageClass.STANDARD;
        private AccessTier accessTier = AccessTier.HOT;
        private CannedAcl acl = CannedAcl.PRIVATE;
        private UserMetadata metadata = new UserMetadata(Map.of());
        private Tags tags = Tags.empty();
        private Encryption encryption = Encryption.none();
        private Retention retention = new Retention(false, null);
        private String idempotencyKey;
        private boolean overwriteIfExists = true;
        private boolean computeChecksum = true;
        private ContentHash expectedHash;

        public Builder(BlobId id){ this.id = id; }
        public Builder data(InputStream is, long len){ this.data = is; this.contentLength = len; return this; }
        public Builder contentType(String ct){ this.contentType = ct; return this; }
        public Builder storageClass(StorageClass sc){ this.storageClass = sc; return this; }
        public Builder accessTier(AccessTier t){ this.accessTier = t; return this; }
        public Builder acl(CannedAcl a){ this.acl = a; return this; }
        public Builder metadata(Map<String,String> m){ this.metadata = new UserMetadata(m); return this; }
        public Builder tags(List<BlobTag> tags){ this.tags = new Tags(tags); return this; }
        public Builder encryption(Encryption e){ this.encryption = e; return this; }
        public Builder retention(Retention r){ this.retention = r; return this; }
        public Builder idempotencyKey(String k){ this.idempotencyKey = k; return this; }
        public Builder overwriteIfExists(boolean o){ this.overwriteIfExists = o; return this; }
        public Builder computeChecksum(boolean c){ this.computeChecksum = c; return this; }
        public Builder expectedHash(ContentHash h){ this.expectedHash = h; return this; }
        public BlobWriteRequest build(){
            Objects.requireNonNull(id); Objects.requireNonNull(data);
            return new BlobWriteRequest(id, data, contentLength, contentType, storageClass, accessTier, acl, metadata, tags, encryption, retention, idempotencyKey, overwriteIfExists, computeChecksum, expectedHash);
        }
    }
}

