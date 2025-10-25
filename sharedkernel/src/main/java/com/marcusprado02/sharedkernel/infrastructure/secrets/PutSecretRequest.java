package com.marcusprado02.sharedkernel.infrastructure.secrets;

import java.time.Instant;
import java.util.Map;

public record PutSecretRequest(
        SecretId id,
        SecretType type,
        Object value,               // String/Map/byte[]
        Map<String,String> tags,
        boolean createIfMissing,
        boolean overwriteCurrent,
        String  expectedEtag,       // CAS otimista
        Instant expiresAt
) {
    public static Builder of(SecretId id){ return new Builder(id); }
    public static final class Builder {
        private final SecretId id;
        private SecretType type = SecretType.STRING;
        private Object value;
        private Map<String,String> tags = Map.of();
        private boolean createIfMissing = true;
        private boolean overwriteCurrent = true;
        private String expectedEtag;
        private Instant expiresAt;
        private Builder(SecretId id){ this.id = id; }
        public Builder type(SecretType t){ this.type=t; return this; }
        public Builder value(Object v){ this.value=v; return this; }
        public Builder tags(Map<String,String> t){ this.tags=t; return this; }
        public Builder createIfMissing(boolean b){ this.createIfMissing=b; return this; }
        public Builder overwrite(boolean b){ this.overwriteCurrent=b; return this; }
        public Builder expectedEtag(String e){ this.expectedEtag=e; return this; }
        public Builder expiresAt(Instant i){ this.expiresAt=i; return this; }
        public PutSecretRequest build(){ return new PutSecretRequest(id,type,value,tags,createIfMissing,overwriteCurrent,expectedEtag,expiresAt); }
    }
}