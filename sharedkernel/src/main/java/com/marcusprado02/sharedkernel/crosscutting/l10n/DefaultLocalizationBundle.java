package com.marcusprado02.sharedkernel.crosscutting.l10n;


import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.*;

public final class DefaultLocalizationBundle implements LocalizationBundle {

    private final BundleStore store;
    private final MessageFormatter formatter;
    private final MissingKeyPolicy missingKeyPolicy;
    private final ConcurrentMap<CacheKey, Optional<String>> cache = new ConcurrentHashMap<>();
    private final ConcurrentMap<FormatKey, java.text.MessageFormat> mfCache = new ConcurrentHashMap<>();

    public DefaultLocalizationBundle(BundleStore store, MessageFormatter formatter, MissingKeyPolicy policy) {
        this.store = Objects.requireNonNull(store);
        this.formatter = Objects.requireNonNull(formatter);
        this.missingKeyPolicy = Objects.requireNonNull(policy);
    }

    @Override public String get(String key, LocalizationContext ctx, Object... args) {
        String pattern = raw(key, ctx).orElseGet(() -> switch (missingKeyPolicy) {
            case RETURN_KEY -> key;
            case RETURN_MARKED -> "??" + key + "??";
            case THROW -> { throw new MissingMessageException(key); }
        });
        return formatter.format(pattern, ctx, args);
    }

    @Override public Optional<String> raw(String key, LocalizationContext ctx) {
        for (var p : FallbackChain.sequence(ctx.locale(), ctx.tenant())) {
            var ck = new CacheKey(p.tenant(), p.locale(), key);
            var opt = cache.computeIfAbsent(ck, k -> store.getMessage(k.tenant(), k.locale(), k.key()));
            if (opt.isPresent()) return opt;
        }
        return Optional.empty();
    }

    // Helpers
    private record CacheKey(String tenant, Locale locale, String key) {}
    private record FormatKey(Locale locale, String pattern) {}
}
