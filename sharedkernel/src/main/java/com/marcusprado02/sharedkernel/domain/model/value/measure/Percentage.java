package com.marcusprado02.sharedkernel.domain.model.value.measure;


import java.math.BigDecimal;
import java.math.RoundingMode;

import com.marcusprado02.sharedkernel.domain.model.value.AbstractValueObject;

public final class Percentage extends AbstractValueObject {
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE = BigDecimal.ONE;

    private final BigDecimal value; // canônico: 0.0 .. 1.0

    private Percentage(BigDecimal v) {
        if (v.compareTo(ZERO) < 0 || v.compareTo(ONE) > 0) {
            throw new IllegalArgumentException("percentage must be in [0,1]");
        }
        this.value = v.stripTrailingZeros();
    }

    public static Percentage ofFraction(BigDecimal fraction) { return new Percentage(fraction); }
    public static Percentage ofPercent(double percent) { // 0..100
        return new Percentage(BigDecimal.valueOf(percent).divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_EVEN));
    }

    public BigDecimal asFraction() { return value; }
    public BigDecimal asPercent() { return value.multiply(BigDecimal.valueOf(100)); }

    public BigDecimal applyTo(BigDecimal base) {
        return base.multiply(value);
    }

    @Override protected Object[] equalityComponents() { return new Object[]{ value }; }
}
