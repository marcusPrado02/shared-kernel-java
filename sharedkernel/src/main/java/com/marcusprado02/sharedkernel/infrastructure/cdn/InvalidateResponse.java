package com.marcusprado02.sharedkernel.infrastructure.cdn;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record InvalidateResponse(
        String provider,            // CloudFront, Cloudflare, etc.
        String distributionId,
        String requestId,           // id da requisição no provedor
        Instant submittedAt,
        String status,              // InProgress/Completed
        List<Target> acceptedTargets,
        Map<String,Object> raw      // payload do provedor
){}
