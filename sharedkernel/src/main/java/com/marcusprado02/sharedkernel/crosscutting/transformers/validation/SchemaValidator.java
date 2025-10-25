package com.marcusprado02.sharedkernel.crosscutting.transformers.validation;

import javax.naming.directory.SchemaViolationException;

public interface SchemaValidator<T> { void validate(T value) throws SchemaViolationException; }

