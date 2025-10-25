package com.marcusprado02.sharedkernel.infrastructure.email.core;

import java.util.concurrent.Callable;

public interface RateLimiter {
	<T> T acquire(Callable<T> c);

	static RateLimiter noop() {
		return new RateLimiter() {
			@Override
			public <T> T acquire(Callable<T> c) {
				try {
					return c.call();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};
	}
}
