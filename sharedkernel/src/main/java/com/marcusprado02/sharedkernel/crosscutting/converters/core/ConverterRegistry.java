package com.marcusprado02.sharedkernel.crosscutting.converters.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ConverterRegistry {
    private final Map<TypePair<?,?>, Converter<?,?>> map = new ConcurrentHashMap<>();

    public <S,T> void register(Class<S> s, Class<T> t, Converter<S,T> c) {
        map.put(TypePair.of(s,t), c);
    }

    @SuppressWarnings("unchecked")
    public <S,T> Converter<S,T> get(Class<S> s, Class<T> t) {
        var c = (Converter<S,T>) map.get(TypePair.of(s,t));
        if (c == null) throw new ConversionException("No converter "+s.getName()+" -> "+t.getName());
        return c;
    }

    // compõe A->B->C dado (A->B) no registry + (B->C) passado
    public static <A,B,C> Converter<A,C> compose(Converter<A,B> ab, Converter<B,C> bc) {
        return a -> bc.convert(ab.convert(a));
    }
}

