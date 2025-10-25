package com.marcusprado02.sharedkernel.infrastructure.maptile.core;

public interface CircuitBreaker<T> {
	T protect(java.util.concurrent.Callable<T> c);

	static <T> CircuitBreaker<T> noOp() {
		return c -> {
			try {
				return c.call();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}
}

