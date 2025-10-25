package com.marcusprado02.sharedkernel.crosscutting.generators.core;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.Flow.*;
import java.util.function.*;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.*;

public record GenerationContext(
        String purpose,
        String namespace,
        OptionalLong deterministicSeed,
        Clock clock,
        Locale locale,
        RandomGenerator rng,
        Map<String, Object> attributes
) {
    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String purpose = "default";
        private String namespace = "default";
        private OptionalLong seed = OptionalLong.empty();
        private Clock clock = Clock.systemUTC();
        private Locale locale = Locale.ROOT;
        private RandomGenerator rng = RandomGenerator.getDefault();
        private final Map<String,Object> attrs = new ConcurrentHashMap<>();

        public Builder purpose(String p){this.purpose=p;return this;}
        public Builder namespace(String n){this.namespace=n;return this;}

        public Builder deterministicSeed(long s){
            this.seed = OptionalLong.of(s);
            // Java 17+: use RandomGeneratorFactory
            this.rng = RandomGeneratorFactory.of("L64X256MixRandom").create(s);
            return this;
        }

        public Builder clock(Clock c){this.clock=c;return this;}
        public Builder locale(Locale l){this.locale=l;return this;}
        public Builder attribute(String k,Object v){attrs.put(k,v);return this;}

        public GenerationContext build(){
            return new GenerationContext(
                purpose, namespace, seed, clock, locale, rng, Map.copyOf(attrs)
            );
        }
    }
}
