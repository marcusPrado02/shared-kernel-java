package com.marcusprado02.sharedkernel.crosscutting.helpers.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class GzipUtil {
  private GzipUtil(){}
  public static byte[] gzip(byte[] data) throws IOException {
    try (var baos = new ByteArrayOutputStream(); var gz = new GZIPOutputStream(baos)) {
      gz.write(data); gz.finish(); return baos.toByteArray();
    }
  }
  public static byte[] gunzip(byte[] gzData) throws IOException {
    try (var gis = new GZIPInputStream(new ByteArrayInputStream(gzData))) {
      return gis.readAllBytes();
    }
  }
}

