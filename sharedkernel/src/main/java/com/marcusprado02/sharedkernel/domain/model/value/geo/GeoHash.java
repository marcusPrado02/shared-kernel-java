package com.marcusprado02.sharedkernel.domain.model.value.geo;

import com.marcusprado02.sharedkernel.domain.model.value.AbstractValueObject;

public final class GeoHash extends AbstractValueObject {
    private static final String ALPHABET = "0123456789bcdefghjkmnpqrstuvwxyz";
    private final String value;

    private GeoHash(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("geohash blank");
        for (char c : value.toLowerCase().toCharArray()) {
            if (ALPHABET.indexOf(c) < 0) throw new IllegalArgumentException("invalid geohash char: " + c);
        }
        this.value = value.toLowerCase();
    }

    public static GeoHash of(String value) { return new GeoHash(value); }
    public String value() { return value; }

    public GeoHash parent() {
        if (value.length() <= 1) throw new IllegalStateException("no parent");
        return new GeoHash(value.substring(0, value.length() - 1));
    }

    @Override protected Object[] equalityComponents() { return new Object[]{ value }; }
}
