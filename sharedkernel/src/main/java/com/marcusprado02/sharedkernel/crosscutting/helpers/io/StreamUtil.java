package com.marcusprado02.sharedkernel.crosscutting.helpers.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Objects;

public final class StreamUtil {
  private static final int BUF = 8192;
  private StreamUtil() {}

  public static long copy(InputStream in, OutputStream out) throws IOException {
    return copy(in, out, BUF);
  }
  public static long copy(InputStream in, OutputStream out, int bufSize) throws IOException {
    in = Objects.requireNonNull(in); out = Objects.requireNonNull(out);
    byte[] buf = new byte[Math.max(1024, bufSize)];
    long total = 0; int r;
    while ((r = in.read(buf)) != -1) { out.write(buf, 0, r); total += r; }
    return total;
  }

  public static String slurp(InputStream in, Charset cs, long maxBytes) throws IOException {
    try (var baos = new ByteArrayOutputStream()) {
      long n = copyLimited(in, baos, maxBytes);
      return baos.toString(cs.name());
    }
  }

  public static long copyLimited(InputStream in, OutputStream out, long max) throws IOException {
    long total = 0; byte[] buf = new byte[BUF]; int r;
    while ((r = in.read(buf)) != -1) {
      if (total + r > max) throw new IOException("payload too large");
      out.write(buf, 0, r); total += r;
    }
    return total;
  }
}

