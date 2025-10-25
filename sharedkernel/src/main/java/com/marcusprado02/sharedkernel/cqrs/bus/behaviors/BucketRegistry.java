package com.marcusprado02.sharedkernel.cqrs.bus.behaviors;

import io.github.bucket4j.*;

public interface BucketRegistry { Bucket get(String key); }
