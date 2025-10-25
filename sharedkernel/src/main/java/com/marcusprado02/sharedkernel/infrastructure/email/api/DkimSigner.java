package com.marcusprado02.sharedkernel.infrastructure.email.api;

public interface DkimSigner {
    /** Assina a mensagem conforme RFC 6376 (header/body). */
    SignedEmail sign(SignedEmail unsigned, DkimConfig config);
}

