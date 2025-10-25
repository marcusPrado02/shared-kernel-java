package com.marcusprado02.sharedkernel.infrastructure.blob;

import java.time.Instant;

public record Retention(
        boolean legalHold,      // true => legal hold habilitado
        Instant retainUntil     // WORM/Retention até data
){}
