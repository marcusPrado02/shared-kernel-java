package com.marcusprado02.sharedkernel.infrastructure.upload.spi;

import java.io.InputStream;

public interface Digest {
    String sha256(InputStream data); // stream é consumido (use buffering eficiente)
}