package com.marcusprado02.sharedkernel.adapters.in.rest.versioning;

import java.io.IOException;

import com.marcusprado02.sharedkernel.cqrs.timemachine.ReplayPlan.Filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public final class DeprecationFilter implements jakarta.servlet.Filter {
    private final ApiVersion until;
    private final String sunsetRfc1123; private final String moreInfoUrl;
    public DeprecationFilter(ApiVersion lastSupported, java.time.ZonedDateTime sunsetAt, String url){
        this.until = lastSupported; this.sunsetRfc1123 = java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME.format(sunsetAt); this.moreInfoUrl = url;
    }
    @Override public void doFilter(ServletRequest sreq, ServletResponse sres, FilterChain chain) throws IOException, ServletException {
        chain.doFilter(sreq, sres);
        var req = (HttpServletRequest)sreq; var resp = (HttpServletResponse)sres;
        var d = (VersionDecision) req.getAttribute(VersionNegotiationFilter.ATTR_DECISION);
        if (d!=null && d.served.major() <= until.major()){
            resp.addHeader("Deprecation", "true");
            resp.addHeader("Sunset", sunsetRfc1123);
            resp.addHeader("Link", "<"+moreInfoUrl+">; rel=\"deprecation\"");
        }
    }
}

