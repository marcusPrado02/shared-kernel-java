package com.marcusprado02.sharedkernel.infrastructure.upload.spi;

import java.io.InputStream;

public interface VirusScanner {
    /** Deve lançar exceção se arquivo/chunk estiver infectado. */
    void scan(InputStream data) throws Exception;
}

