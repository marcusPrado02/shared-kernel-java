package com.marcusprado02.sharedkernel.infrastructure.search;

import java.util.List;
import java.util.Map;

import com.marcusprado02.sharedkernel.infrastructure.search.Criterion.BoolCriterion;
import com.marcusprado02.sharedkernel.infrastructure.search.Criterion.FieldCriterion;
import com.marcusprado02.sharedkernel.infrastructure.search.Criterion.ScriptCriterion;

public sealed interface Criterion permits FieldCriterion, BoolCriterion, ScriptCriterion {
    record FieldCriterion(String path, Operator op, Object value, Object auxValue) implements Criterion {}
    record BoolCriterion(List<Criterion> must, List<Criterion> should, List<Criterion> mustNot, Integer minimumShouldMatch) implements Criterion {}
    record ScriptCriterion(String script, Map<String, Object> params) implements Criterion {}
}
