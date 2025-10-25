package com.marcusprado02.sharedkernel.crosscutting.l10n;

import java.util.Optional;

/** Resolve uma chave em uma mensagem formatada. */
public interface LocalizationBundle {
    String get(String key, LocalizationContext ctx, Object... args);
    /** Retorna opcional cru (sem formatação) para introspecção/testes. */
    Optional<String> raw(String key, LocalizationContext ctx);
}
