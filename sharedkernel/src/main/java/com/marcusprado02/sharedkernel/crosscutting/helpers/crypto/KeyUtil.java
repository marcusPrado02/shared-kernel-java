package com.marcusprado02.sharedkernel.crosscutting.helpers.crypto;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public final class KeyUtil {
  private KeyUtil(){}
  public static PrivateKey readRsaPrivatePem(String pem) {
    try {
      String c = pem.replaceAll("-----\\w+ RSA PRIVATE KEY-----", "").replaceAll("\\s", "");
      byte[] pkcs8 = Base64.getDecoder().decode(c);
      return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(pkcs8));
    } catch (Exception e) { throw new IllegalArgumentException("invalid RSA key", e); }
  }
  public static PublicKey readRsaPublicPem(String pem) {
    try {
      String c = pem.replaceAll("-----\\w+ PUBLIC KEY-----", "").replaceAll("\\s", "");
      byte[] spki = Base64.getDecoder().decode(c);
      return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(spki));
    } catch (Exception e) { throw new IllegalArgumentException("invalid RSA pubkey", e); }
  }
}
