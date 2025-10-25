package com.marcusprado02.sharedkernel.crosscutting.l10n;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/** Fornece acesso ao armazenamento de bundles (por locale/tenant). */
public interface BundleStore {
    Optional<String> getMessage(String tenant, Locale locale, String key);
    Set<String> keys(String tenant, Locale locale);
}