package com.marcusprado02.sharedkernel.crosscutting.helpers.io;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class ChecksumUtil {
  private ChecksumUtil(){}
  public static String sha256Hex(byte[] data) {
    try {
      var md = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(md.digest(data));
    } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
  }
}

