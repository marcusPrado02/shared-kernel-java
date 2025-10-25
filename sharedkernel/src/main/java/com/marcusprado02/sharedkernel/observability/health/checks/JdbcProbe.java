package com.marcusprado02.sharedkernel.observability.health.checks;

import java.net.http.*;
import java.net.*;
import java.time.*;
import java.util.Map;

import javax.sql.DataSource;

import com.marcusprado02.sharedkernel.observability.health.*;

import java.sql.Connection;

public final class JdbcProbe implements ProbeCheck {
    private final DataSource ds; private final Duration timeout;
    public JdbcProbe(DataSource ds, Duration timeout){ this.ds=ds; this.timeout=timeout; }
    @Override public ProbeResult check() {
        var start = System.nanoTime();
        try (Connection c = ds.getConnection()) {
            boolean valid = c.isValid((int) timeout.toSeconds());
            var dt = Duration.ofNanos(System.nanoTime()-start);
            return valid ? ProbeResult.up(dt) : ProbeResult.degraded("jdbc_not_valid", Map.of("timeout", timeout), dt);
        } catch (Exception e) {
            var dt = Duration.ofNanos(System.nanoTime()-start);
            return ProbeResult.down("jdbc_error", Map.of("error", e.getClass().getSimpleName(), "msg", e.getMessage()), dt);
        }
    }
    @Override public String name() { return "jdbc"; }
}
