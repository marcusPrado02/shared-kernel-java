package com.marcusprado02.sharedkernel.adapters.in.rest.versioning;

import java.util.ArrayList;
import java.util.List;

public final class VersionNegotiator {
    private final VersionCatalog catalog;
    private final String baseProfile; // ex.: https://api.example.com/profiles/

    public VersionNegotiator(VersionCatalog catalog, String baseProfile){
        this.catalog = catalog; this.baseProfile = baseProfile.endsWith("/")? baseProfile : baseProfile + "/";
    }

    /** Negocia versão com base em Accept, Accept-Version, X-Api-Version, default. */
    public VersionDecision decide(String logicalType, String acceptHeader, String acceptVersionHeader, String xApiVersionHeader){
        // 1) requested a partir de headers (ordem de prioridade)
        ApiVersion requested = null;
        if (xApiVersionHeader != null) requested = ApiVersion.parse(xApiVersionHeader);
        else if (acceptVersionHeader != null) requested = ApiVersion.parse(acceptVersionHeader);
        else {
            var choices = AcceptHeader.parse(acceptHeader);
            for (var c : choices){
                var v = AcceptHeader.versionOf(c);
                if (v.isPresent()){ requested = v.get(); break; }
            }
        }
        if (requested == null) requested = catalog.latest(logicalType); // default: latest minor do maior major disponível

        var best = catalog.best(logicalType, requested).orElseThrow(() -> new IllegalArgumentException("unsupported_type_or_version"));
        boolean exact = best.equals(requested);

        var ct = "application/json; profile=\"" + baseProfile + logicalType + ".v" + best + "\"";

        var vary = new ArrayList<>(List.of("Accept","Accept-Version","X-Api-Version"));
        return new VersionDecision(requested, best, logicalType, ct, exact, vary);
    }
}

