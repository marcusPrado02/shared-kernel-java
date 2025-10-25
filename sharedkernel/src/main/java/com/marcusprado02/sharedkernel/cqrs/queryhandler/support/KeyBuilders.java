package com.marcusprado02.sharedkernel.cqrs.queryhandler.support;

import com.marcusprado02.sharedkernel.cqrs.query.Query;
import com.marcusprado02.sharedkernel.cqrs.query.QueryMetadata;

public final class KeyBuilders {
    private KeyBuilders() {}
    public static String cacheKey(Query<?> q, QueryMetadata md, boolean perUser){
        var base = q.getClass().getName() + ":" + Integer.toHexString(q.hashCode());
        if (perUser && md.userId() != null) base += ":u=" + md.userId();
        if (md.tenantId() != null) base += ":t=" + md.tenantId();
        if (md.consistency() != null) base += ":c=" + md.consistency().name();
        return "q:" + base;
    }
}