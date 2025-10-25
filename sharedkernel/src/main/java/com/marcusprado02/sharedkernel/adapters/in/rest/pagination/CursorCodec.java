package com.marcusprado02.sharedkernel.adapters.in.rest.pagination;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Map;

public final class CursorCodec {
    private final ObjectMapper om;
    private final byte[] hmacKey; // opcional; se null, sem assinatura

    public CursorCodec(ObjectMapper om, byte[] hmacKey) {
        this.om = om; this.hmacKey = hmacKey;
    }

    public String encode(CursorPayload payload){
        try {
            var json = om.writeValueAsBytes(payload);
            var b64 = Base64.getUrlEncoder().withoutPadding().encodeToString(json);
            if (hmacKey == null) return b64;
            var sig = hmacSha256(b64, hmacKey);
            return b64 + "." + sig;
        } catch (Exception e){ throw new IllegalStateException(e); }
    }

    public CursorPayload decode(String token){
        try {
            if (token == null || token.isBlank()) return null;
            String[] parts = token.split("\\.");
            String b64 = parts[0];
            if (hmacKey != null) {
                if (parts.length != 2) throw new IllegalArgumentException("cursor signature missing");
                var expected = hmacSha256(b64, hmacKey);
                if (!expected.equals(parts[1])) throw new SecurityException("invalid cursor signature");
            }
            var json = Base64.getUrlDecoder().decode(b64);
            return om.readValue(json, CursorPayload.class);
        } catch (Exception e){ throw new IllegalArgumentException("invalid_cursor", e); }
    }

    private static String hmacSha256(String data, byte[] key) throws Exception {
        var mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(data.getBytes()));
    }

    /** Helper: monta payload com base nos valores da última linha. */
    public CursorPayload makeFromRow(Map<String,Object> sortValues, Direction dir){
        return new CursorPayload(sortValues, dir, java.time.Instant.now());
    }
}
