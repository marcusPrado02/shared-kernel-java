package com.marcusprado02.sharedkernel.crosscutting.converters.impl;

import java.util.Base64;

import com.marcusprado02.sharedkernel.crosscutting.converters.core.BidiConverter;
import com.marcusprado02.sharedkernel.crosscutting.converters.core.ConversionException;
import com.marcusprado02.sharedkernel.crosscutting.converters.core.Converter;

public final class BytesBase64 implements BidiConverter<byte[], String> {
    private final boolean urlSafe;
    public BytesBase64(boolean urlSafe){ this.urlSafe=urlSafe; }
    @Override public String convert(byte[] src) {
        return (urlSafe? Base64.getUrlEncoder().withoutPadding() : Base64.getEncoder()).encodeToString(src);
    }
    @Override public Converter<String, byte[]> inverse() {
        return s -> {
            try { return (urlSafe? Base64.getUrlDecoder() : Base64.getDecoder()).decode(s); }
            catch (Exception e) { throw new ConversionException("Invalid base64", e); }
        };
    }
}
