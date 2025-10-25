package com.marcusprado02.sharedkernel.crosscutting.formatters.id;

import java.util.Locale;
import java.util.UUID;

import com.marcusprado02.sharedkernel.crosscutting.formatters.core.Formatter;

public final class UuidFormatter implements Formatter<UUID> {
    @Override public String format(UUID v) {
        return v.toString().toLowerCase(Locale.ROOT);
    }
}
