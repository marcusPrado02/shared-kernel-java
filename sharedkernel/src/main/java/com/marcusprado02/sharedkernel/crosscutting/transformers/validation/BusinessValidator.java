package com.marcusprado02.sharedkernel.crosscutting.transformers.validation;

import com.marcusprado02.sharedkernel.crosscutting.transformers.core.*;

public interface BusinessValidator<T> { void check(T value, TransformContext ctx); }
