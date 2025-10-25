package com.marcusprado02.sharedkernel.infrastructure.secrets;

import java.util.*;

public final class SecretValue implements AutoCloseable {
    private char[] text;            // preferir char[] a String (GC-friendly para scrubbing)
    private byte[] bytes;           // para binários (certs/PKCS#12/etc.)
    private final String contentType; // "text/plain", "application/json", "application/x-pkcs12"
    public SecretValue(char[] text, String contentType){ this.text=text; this.contentType=contentType; }
    public SecretValue(byte[] bytes, String contentType){ this.bytes=bytes; this.contentType=contentType; }
    public Optional<char[]> text(){ return Optional.ofNullable(text); }
    public Optional<byte[]> bytes(){ return Optional.ofNullable(bytes); }
    public String contentType(){ return contentType; }
    @Override public void close(){ wipe(); }
    public void wipe(){
        if (text!=null) java.util.Arrays.fill(text, '\0');
        if (bytes!=null) java.util.Arrays.fill(bytes, (byte)0);
        text=null; bytes=null;
    }
}