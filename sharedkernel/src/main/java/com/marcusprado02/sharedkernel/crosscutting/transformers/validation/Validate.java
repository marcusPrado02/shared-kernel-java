package com.marcusprado02.sharedkernel.crosscutting.transformers.validation;

import javax.naming.directory.SchemaViolationException;

import com.marcusprado02.sharedkernel.crosscutting.transformers.core.*;
import com.marcusprado02.sharedkernel.domain.service.error.BusinessRuleException;

public final class Validate<T> implements TransformFunction<T,T> {
    private final SchemaValidator<T> schema; private final BusinessValidator<T> rules;
    public Validate(SchemaValidator<T> schema, BusinessValidator<T> rules) { this.schema = schema; this.rules = rules; }
    @Override public TransformResult<T> apply(T in, TransformContext ctx) {
        try { if (schema != null) schema.validate(in); if (rules != null) rules.check(in, ctx); return TransformResult.ok(in); }
        catch (SchemaViolationException e) { return TransformResult.dlq("schema:" + e.getMessage()); }
        catch (Exception e) { return TransformResult.drop("rule:" + e.getMessage()); }
    }
}
