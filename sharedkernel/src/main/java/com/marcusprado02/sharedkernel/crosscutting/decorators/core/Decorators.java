package com.marcusprado02.sharedkernel.crosscutting.decorators.core;

import java.util.List;
import java.util.function.UnaryOperator;

public final class Decorators {
    public static <I,O> Port<I,O> compose(
            Port<I,O> target,
            List<UnaryOperator<Port<I,O>>> layers
    ) {
        Port<I,O> current = target;
        // aplica na ordem declarada (primeiro da lista executa mais "por fora")
        for (int i = layers.size() - 1; i >= 0; i--) {
            current = layers.get(i).apply(current);
        }
        return current;
    }
}

