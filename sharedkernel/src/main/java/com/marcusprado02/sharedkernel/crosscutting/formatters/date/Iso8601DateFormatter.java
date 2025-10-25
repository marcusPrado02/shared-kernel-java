package com.marcusprado02.sharedkernel.crosscutting.formatters.date;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.marcusprado02.sharedkernel.crosscutting.formatters.core.Formatter;

public final class Iso8601DateFormatter implements Formatter<Instant> {
    private final DateTimeFormatter fmt;
    public Iso8601DateFormatter(ZoneId zone) {
        this.fmt = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(zone);
    }
    @Override
    public String format(Instant v) {
        return fmt.format(v);
    }
}

