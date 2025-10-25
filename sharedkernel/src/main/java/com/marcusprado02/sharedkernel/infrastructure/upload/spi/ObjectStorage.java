package com.marcusprado02.sharedkernel.infrastructure.upload.spi;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

public interface ObjectStorage {
    /** Grava um chunk; para S3 multipart pode retornar 'partETag'. */
    String putChunk(String bucket, String storageKey, int partIndex, InputStream data, long size, Map<String,String> headers);
    /** Junta as partes em um único objeto (S3 CompleteMultipartUpload). */
    URI assemble(String bucket, String storageKey, List<String> partEtags);
    /** Upload único (para arquivos pequenos). */
    URI putObject(String bucket, String storageKey, InputStream data, long size, Map<String,String> headers);
    /** Gera URLs presignadas de upload para envio direto do cliente. */
    Map<Integer, URI> presignUploadParts(String bucket, String storageKey, int startIndexInclusive, int count, long partSize, Map<String,String> headers);
    /** Aborta upload multipart (limpeza). */
    void abortMultipart(String bucket, String storageKey);
}
