package com.marcusprado02.sharedkernel.crosscutting.converters.impl;

import java.util.Locale;
import java.util.UUID;

import com.marcusprado02.sharedkernel.crosscutting.converters.core.BidiConverter;
import com.marcusprado02.sharedkernel.crosscutting.converters.core.ConversionException;
import com.marcusprado02.sharedkernel.crosscutting.converters.core.Converter;

public final class UuidToString implements BidiConverter<UUID,String> {
    @Override public String convert(UUID u) { return u.toString().toLowerCase(Locale.ROOT); }
    @Override public Converter<String,UUID> inverse() {
        return s -> {
            try { return UUID.fromString(s.trim()); }
            catch (Exception e) { throw new ConversionException("Invalid UUID: "+safe(s), e); }
        };
    }
    private String safe(String s){ return s==null?"null":s.length()>64?s.substring(0,64)+"…":s; }
}

