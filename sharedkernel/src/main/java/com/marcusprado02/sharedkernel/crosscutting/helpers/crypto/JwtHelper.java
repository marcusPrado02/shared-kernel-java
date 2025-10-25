package com.marcusprado02.sharedkernel.crosscutting.helpers.crypto;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.marcusprado02.sharedkernel.crosscutting.helpers.json.JsonHelper;

public final class JwtHelper {
  private JwtHelper(){}

  public static String signHs256(Map<String,Object> claims, byte[] secret) {
    String header = base64UrlJson(Map.of("alg","HS256","typ","JWT"));
    String payload = base64UrlJson(claims);
    String signingInput = header + "." + payload;
    byte[] sig = hmacSha256(secret, signingInput.getBytes(StandardCharsets.UTF_8));
    return signingInput + "." + base64Url(sig);
  }

  public static boolean verifyHs256(String jwt, byte[] secret) {
    String[] p = jwt.split("\\.");
    if (p.length!=3) return false;
    byte[] expected = hmacSha256(secret, (p[0]+"."+p[1]).getBytes(StandardCharsets.UTF_8));
    return HmacHelper.constantTimeEquals(expected, base64UrlDecode(p[2]));
  }

  public static String signRs256(Map<String,Object> claims, PrivateKey key) {
    String header = base64UrlJson(Map.of("alg","RS256","typ","JWT"));
    String payload = base64UrlJson(claims);
    String signingInput = header + "." + payload;
    byte[] sig = rsaSign(signingInput.getBytes(StandardCharsets.UTF_8), key);
    return signingInput + "." + base64Url(sig);
  }

  public static boolean verifyRs256(String jwt, PublicKey pub) {
    String[] p = jwt.split("\\.");
    if (p.length!=3) return false;
    return rsaVerify((p[0]+"."+p[1]).getBytes(StandardCharsets.UTF_8), base64UrlDecode(p[2]), pub);
  }

  // ---- internos utilitários ----
  private static byte[] hmacSha256(byte[] key, byte[] msg) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(key, "HmacSHA256"));
      return mac.doFinal(msg);
    } catch (GeneralSecurityException e) { throw new IllegalStateException(e); }
  }
  private static byte[] rsaSign(byte[] data, PrivateKey key) {
    try {
      var sig = Signature.getInstance("SHA256withRSA");
      sig.initSign(key); sig.update(data); return sig.sign();
    } catch (Exception e) { throw new IllegalStateException(e); }
  }
  private static boolean rsaVerify(byte[] data, byte[] s, PublicKey k) {
    try {
      var sig = Signature.getInstance("SHA256withRSA");
      sig.initVerify(k); sig.update(data); return sig.verify(s);
    } catch (Exception e) { return false; }
  }
  private static String base64UrlJson(Object o) {
    try { return base64Url(JsonHelper.write(o).getBytes(StandardCharsets.UTF_8)); }
    catch (Exception e) { throw new IllegalArgumentException("json error", e); }
  }
  private static String base64Url(byte[] b) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
  }
  private static byte[] base64UrlDecode(String s) {
    return Base64.getUrlDecoder().decode(s);
  }
}

