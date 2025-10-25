package com.marcusprado02.sharedkernel.domain.model.value.normalization;

import java.text.Normalizer.Form;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

import com.marcusprado02.sharedkernel.domain.model.value.AbstractValueObject;

/**
 * VO que encapsula uma estratégia de normalização de strings.
 * Imutável e com igualdade por valor.
 */
public final class NormalizedString extends AbstractValueObject {

    private final String value;

    private NormalizedString(String normalized) {
        this.value = Objects.requireNonNull(normalized, "normalized must not be null");
    }

    public static NormalizedString of(String raw, Function<String,String> strategy) {
        Objects.requireNonNull(strategy, "strategy must not be null");
        return new NormalizedString(strategy.apply(Objects.requireNonNull(raw, "raw must not be null")));
    }

    public static NormalizedString lowerTrimmed(String raw) {
        return new NormalizedString(raw == null ? null : raw.trim().toLowerCase(Locale.ROOT));
    }

    public static NormalizedString noDiacritics(String raw) {
        if (raw == null) throw new IllegalArgumentException("raw must not be null");
        var norm = Normalizer.normalize(raw, Form.NFD)
                             .replaceAll("\\p{M}", "")
                             .toLowerCase(Locale.ROOT)
                             .trim();
        return new NormalizedString(norm);
    }

    public String value() { return value; }

    @Override
    protected Object[] equalityComponents() { return new Object[]{ value }; }

    @Override
    public String toString() { return value; }
}
