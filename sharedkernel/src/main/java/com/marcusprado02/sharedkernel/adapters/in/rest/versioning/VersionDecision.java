package com.marcusprado02.sharedkernel.adapters.in.rest.versioning;

import java.util.*;

public final class VersionDecision {
    public final ApiVersion requested;
    public final ApiVersion served;
    public final String logicalType;         // ex.: "customer"
    public final String contentType;         // ex.: application/json;profile=".../customer.v1"
    public final boolean exact;              // serviu a versão exata?
    public final List<String> varyOn;        // cabeçalhos/params que devem entrar em Vary

    public VersionDecision(ApiVersion req, ApiVersion served, String logicalType, String contentType, boolean exact, List<String> vary){
        this.requested=req; this.served=served; this.logicalType=logicalType; this.contentType=contentType; this.exact=exact; this.varyOn=vary;
    }
}

