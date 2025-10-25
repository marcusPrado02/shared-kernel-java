package com.marcusprado02.sharedkernel.infrastructure.upload;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;

import com.marcusprado02.sharedkernel.infrastructure.upload.spi.Digest;

import jakarta.xml.bind.DatatypeConverter;

final class HashingInputStream {
    private final InputStream original;
    private final byte[] buffer;
    private final MessageDigest md;
    private long bytes = 0;
    private byte[] cached; // replay buffer opcional para reuso imediato

    HashingInputStream(InputStream in, Digest dig){
        this.original = in;
        this.buffer = new byte[8192];
        try { this.md = MessageDigest.getInstance("SHA-256"); }
        catch (Exception e){ throw new RuntimeException(e); }
    }

    InputStream peek(){ return original; }

    InputStream rewind(){
        if (cached != null) return new ByteArrayInputStream(cached);
        // como exemplo simples: materializa em memória (para produção troque por temp file)
        // Em produção: use arquivo temporário (e.g. DiskBufferingInputStream) para chunks grandes.
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int read;
            while ((read = original.read(buffer)) != -1) {
                baos.write(buffer, 0, read);
                bytes += read;
                md.update(buffer, 0, read);
            }
            cached = baos.toByteArray();
            return new ByteArrayInputStream(cached);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    String sha256(){ return bytes == 0 ? "" : DatatypeConverter.printHexBinary(md.digest()).toLowerCase(); }
    long bytesRead(){ return bytes; }
}

