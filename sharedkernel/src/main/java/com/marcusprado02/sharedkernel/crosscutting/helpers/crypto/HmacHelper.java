package com.marcusprado02.sharedkernel.crosscutting.helpers.crypto;

import java.security.GeneralSecurityException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class HmacHelper {
  private HmacHelper(){}
  public static byte[] hmacSha256(byte[] key, byte[] msg) {
    try {
      var mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(key, "HmacSHA256"));
      return mac.doFinal(msg);
    } catch (GeneralSecurityException e) { throw new IllegalStateException(e); }
  }
  public static boolean constantTimeEquals(byte[] a, byte[] b) {
    if (a == null || b == null) return false;
    if (a.length != b.length) return false;
    int r = 0; for (int i=0; i<a.length; i++) r |= a[i] ^ b[i];
    return r == 0;
  }
}
