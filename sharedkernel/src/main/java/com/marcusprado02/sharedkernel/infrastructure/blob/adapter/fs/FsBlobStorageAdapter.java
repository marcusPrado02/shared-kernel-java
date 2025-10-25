package com.marcusprado02.sharedkernel.infrastructure.blob.adapter.fs;


import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

import com.marcusprado02.sharedkernel.infrastructure.blob.BlobId;
import com.marcusprado02.sharedkernel.infrastructure.blob.BlobReadRequest;
import com.marcusprado02.sharedkernel.infrastructure.blob.BlobWriteRequest;
import com.marcusprado02.sharedkernel.infrastructure.blob.ContentHash;
import com.marcusprado02.sharedkernel.infrastructure.blob.DownloadResult;
import com.marcusprado02.sharedkernel.infrastructure.blob.ListRequest;
import com.marcusprado02.sharedkernel.infrastructure.blob.ListResult;
import com.marcusprado02.sharedkernel.infrastructure.blob.PresignRequest;
import com.marcusprado02.sharedkernel.infrastructure.blob.PresignResult;
import com.marcusprado02.sharedkernel.infrastructure.blob.StatResult;
import com.marcusprado02.sharedkernel.infrastructure.blob.UploadResult;
import com.marcusprado02.sharedkernel.infrastructure.blob.adapter.BaseBlobStorageAdapter;

public class FsBlobStorageAdapter extends BaseBlobStorageAdapter {

    private final Path root;

    public FsBlobStorageAdapter(io.opentelemetry.api.trace.Tracer tracer,
                                io.micrometer.core.instrument.MeterRegistry meter,
                                io.github.resilience4j.retry.Retry retry,
                                io.github.resilience4j.circuitbreaker.CircuitBreaker cb,
                                Path root) {
        super(tracer, meter, retry, cb);
        this.root = root;
    }

    private Path resolve(BlobId id){ return root.resolve(id.bucket()).resolve(id.key()); }

    @Override protected UploadResult doPut(BlobWriteRequest req) {
        try {
            Path p = resolve(req.id());
            Files.createDirectories(p.getParent());
            try (var out = Files.newOutputStream(p, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                req.data().transferTo(out);
            }
            return new UploadResult(req.id(), null, null, req.contentLength(), req.contentType(), req.metadata().values(), Instant.now(), p.toUri().toURL());
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    @Override protected DownloadResult doGet(BlobReadRequest req) {
        try {
            Path p = resolve(req.id());
            if (!Files.exists(p)) throw new FileNotFoundException(p.toString());
            long size = Files.size(p);
            InputStream is;
            if (req.range() != null) {
                var r = req.range();
                long start = r.start();
                long end = r.end() == null ? size - 1 : r.end();
                var raf = new RandomAccessFile(p.toFile(), "r");
                raf.seek(start);
                long length = (end - start + 1);
                is = new BoundedInputStream(Channels.newInputStream(raf.getChannel()), length, raf);
                return new DownloadResult(req.id(), is, length, Files.probeContentType(p), null, null, Map.of());
            } else {
                is = Files.newInputStream(p, StandardOpenOption.READ);
                return new DownloadResult(req.id(), is, size, Files.probeContentType(p), null, null, Map.of());
            }
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    @Override public StatResult stat(BlobId id) {
        try {
            Path p = resolve(id);
            if (!Files.exists(p)) return new StatResult(id, false, 0, null, null, null, null, Map.of(), List.of());
            return new StatResult(id, true, Files.size(p), Files.probeContentType(p), null, null, Files.getLastModifiedTime(p).toInstant(), Map.of(), List.of());
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    @Override public boolean exists(BlobId id){ return Files.exists(resolve(id)); }
    @Override public void delete(BlobId id){ try { Files.deleteIfExists(resolve(id)); } catch (IOException e){ throw new RuntimeException(e); } }
    @Override public void deletePrefix(String bucket, String prefix){
        try { Files.walk(root.resolve(bucket)).filter(p -> p.toString().contains("/"+prefix)).forEach(p -> {try{Files.deleteIfExists(p);}catch(Exception ignored){}}); }
        catch (IOException e){ throw new RuntimeException(e); }
    }
    @Override public void copy(BlobId s, BlobId t, boolean overwrite, Map<String,String> m){
        try {
            Path sp = resolve(s), tp = resolve(t);
            Files.createDirectories(tp.getParent());
            Files.copy(sp, tp, overwrite ? new CopyOption[]{StandardCopyOption.REPLACE_EXISTING} : new CopyOption[]{});
        } catch (IOException e){ throw new RuntimeException(e); }
    }
    @Override public void move(BlobId s, BlobId t, boolean overwrite){
        try {
            Path sp = resolve(s), tp = resolve(t);
            Files.createDirectories(tp.getParent());
            Files.move(sp, tp, overwrite ? new CopyOption[]{StandardCopyOption.REPLACE_EXISTING} : new CopyOption[]{});
        } catch (IOException e){ throw new RuntimeException(e); }
    }
    @Override public ListResult list(ListRequest req){
        try {
            Path base = root.resolve(req.bucket()).resolve(req.prefix() == null ? "" : req.prefix());
            if (!Files.exists(base)) return new ListResult(List.of(), null, false);
            var keys = new ArrayList<String>();
            try (var st = Files.walk(base)) {
                st.filter(Files::isRegularFile).forEach(p -> {
                    String key = root.resolve(req.bucket()).relativize(p).toString().replace('\\','/');
                    keys.add(key);
                });
            }
            return new ListResult(keys, null, false);
        } catch (IOException e){ throw new RuntimeException(e); }
    }

    // multipart: simplificado
    @Override public String createMultipartUpload(BlobWriteRequest init){ return UUID.randomUUID().toString(); }
    @Override public void uploadPart(BlobId id, String uploadId, int partNumber, InputStream data, long size, ContentHash md5){ /* persistir partes em dir tmp */ }
    @Override public UploadResult completeMultipartUpload(BlobId id, String uploadId, Map<Integer, String> parts){ /* juntar */ return stat(id).exists()? new UploadResult(id, null,null,0,null,Map.of(),Instant.now(),null): doPut(BlobWriteRequest.to(id).data(InputStream.nullInputStream(),0).build()); }
    @Override public void abortMultipartUpload(BlobId id, String uploadId){}

    @Override public PresignResult presign(PresignRequest req){
        try {
            URL url = resolve(req.id()).toUri().toURL(); // sem assinatura; útil só em dev
            return new PresignResult(url, Instant.now().plus(req.ttl()), Map.of());
        } catch (Exception e){ throw new RuntimeException(e); }
    }

    // Helper para range com RandomAccessFile
    static final class BoundedInputStream extends InputStream {
        private final InputStream delegate;
        private long remaining;
        private final RandomAccessFile raf;
        BoundedInputStream(InputStream d, long len, RandomAccessFile raf){ this.delegate = d; this.remaining = len; this.raf = raf; }
        @Override public int read() throws IOException { if (remaining<=0) return -1; int b = delegate.read(); if (b!=-1) remaining--; return b; }
        @Override public int read(byte[] b, int off, int len) throws IOException { if (remaining<=0) return -1; int toRead = (int)Math.min(len, remaining); int n = delegate.read(b, off, toRead); if (n>0) remaining -= n; return n; }
        @Override public void close() throws IOException { delegate.close(); raf.close(); }
    }
}
