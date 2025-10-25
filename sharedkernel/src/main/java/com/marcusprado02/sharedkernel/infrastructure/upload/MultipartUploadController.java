package com.marcusprado02.sharedkernel.infrastructure.upload;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/uploads")
@RequiredArgsConstructor
public class MultipartUploadController {

    private final MultipartUploadService service;

    @PostMapping("/init")
    public ResponseEntity<?> init(@RequestBody InitRequest req) {
        var spec = new UploadSpec(
                req.filename(), req.contentType(), req.expectedSize(),
                req.expectedSha256(), req.metadata(), req.directToCloud()
        );
        var out = service.init(spec);
        return ResponseEntity.created(out.location()).body(out); 
    }

    /** Cliente envia (index, size, sha256, file) – para "gateway upload". */
    @PostMapping(path="/{uploadId}/chunks", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadChunk(
            @PathVariable String uploadId,
            @RequestPart("index") Integer index,
            @RequestPart(value="size", required=false) Long size,
            @RequestPart(value="sha256", required=false) String sha256,
            @RequestPart("file") MultipartFile file
    ) throws Exception {
        var ack = service.uploadChunk(uploadId, index, size==null?-1:size, sha256, file.getInputStream());
        return ResponseEntity.accepted().body(ack);
    }

    /** Cliente pede URLs presignadas para enviar direto ao storage. */
    @PostMapping("/{uploadId}/presign")
    public ResponseEntity<?> presign(@PathVariable String uploadId,
                                     @RequestParam int startIndex,
                                     @RequestParam int count,
                                     @RequestParam long partSize){
        var map = service.presign(uploadId, startIndex, count, partSize);
        return ResponseEntity.ok(map);
    }

    /** Cliente informa que terminou (e.g., com a contagem esperada). */
    @PostMapping("/{uploadId}/complete")
    public ResponseEntity<?> complete(@PathVariable String uploadId,
                                      @RequestParam(required=false) Integer totalChunks){
        var result = service.complete(uploadId, totalChunks);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{uploadId}")
    public ResponseEntity<?> abort(@PathVariable String uploadId, @RequestParam(required=false) String reason){
        service.abort(uploadId, reason);
        return ResponseEntity.noContent().build();
    }

    // DTOs REST
    public record InitRequest(String filename, String contentType, long expectedSize,
                              String expectedSha256, Map<String,String> metadata, boolean directToCloud) {}
}
