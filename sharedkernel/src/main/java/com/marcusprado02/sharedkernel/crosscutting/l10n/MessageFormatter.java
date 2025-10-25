package com.marcusprado02.sharedkernel.crosscutting.l10n;

/** Estratégia de formatação (MessageFormat/ICU4J/etc.). */
@FunctionalInterface
public interface MessageFormatter {
    String format(String pattern, LocalizationContext ctx, Object... args);
}