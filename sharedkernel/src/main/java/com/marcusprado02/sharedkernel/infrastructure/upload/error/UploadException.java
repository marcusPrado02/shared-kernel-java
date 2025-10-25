package com.marcusprado02.sharedkernel.infrastructure.upload.error;

public class UploadException extends RuntimeException {
    private final String code;
    public UploadException(String code) {
        super(code);
        this.code = code;
    }
    public UploadException(String code, Throwable cause) {
        super(code, cause);
        this.code = code;
    }
    public String code() { return code; }
}
