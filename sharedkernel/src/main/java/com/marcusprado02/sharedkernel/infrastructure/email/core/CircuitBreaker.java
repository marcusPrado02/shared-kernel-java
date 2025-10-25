package com.marcusprado02.sharedkernel.infrastructure.email.core;

public interface CircuitBreaker {
	<T> T protect(java.util.concurrent.Callable<T> c);
	static CircuitBreaker noOp() {
		return new CircuitBreaker() {
			@Override
			public <T> T protect(java.util.concurrent.Callable<T> c) {
				try {
					return c.call();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};
	}
}
