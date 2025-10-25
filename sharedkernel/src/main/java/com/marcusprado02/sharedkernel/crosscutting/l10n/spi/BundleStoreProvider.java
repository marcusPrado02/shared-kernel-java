package com.marcusprado02.sharedkernel.crosscutting.l10n.spi;

import java.net.URI;
import java.util.Map;

import com.marcusprado02.sharedkernel.crosscutting.l10n.BundleStore;

public interface BundleStoreProvider {
    boolean supports(URI uri);
    BundleStore create(URI uri, Map<String,?> defaults);
}

