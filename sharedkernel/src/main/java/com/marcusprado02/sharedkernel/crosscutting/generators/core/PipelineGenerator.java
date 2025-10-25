package com.marcusprado02.sharedkernel.crosscutting.generators.core;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

public final class PipelineGenerator<T> implements Generator<T> {
    private final Generator<T> source;
    private final List<Transform<T,T>> transforms;
    private final List<Validator<T>> validators;

    public PipelineGenerator(Generator<T> source, List<Transform<T,T>> tr, List<Validator<T>> val) {
        this.source = source; this.transforms = List.copyOf(tr); this.validators = List.copyOf(val);
    }

    @Override public T generate(GenerationContext ctx) {
        T v = source.generate(ctx);
        for (Transform<T,T> t : transforms) v = t.apply(v, ctx);
        for (Validator<T> val : validators) val.validate(v, ctx);
        return v;
    }

    public static final class Builder<T> {
        private Generator<T> source;
        private final java.util.ArrayList<Transform<T,T>> tr = new java.util.ArrayList<>();
        private final java.util.ArrayList<Validator<T>> val = new java.util.ArrayList<>();
        public Builder<T> source(Generator<T> g){this.source=g;return this;}
        public Builder<T> transform(Transform<T,T> t){tr.add(t);return this;}
        public Builder<T> validate(Validator<T> v){val.add(v);return this;}
        public PipelineGenerator<T> build(){return new PipelineGenerator<>(source,tr,val);}
    }
}