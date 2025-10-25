package com.marcusprado02.sharedkernel.crosscutting.converters.core;

// Identifica o par <S,T> de forma tipo-segura
public record TypePair<S,T>(Class<S> source, Class<T> target) {
    public static <S,T> TypePair<S,T> of(Class<S> s, Class<T> t) { return new TypePair<>(s,t); }
}
