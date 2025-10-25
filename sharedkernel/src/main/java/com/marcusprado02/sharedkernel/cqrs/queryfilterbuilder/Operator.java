package com.marcusprado02.sharedkernel.cqrs.queryfilterbuilder;

import java.util.*;
import java.util.function.Function;

public enum Operator {
    EQ, NE, GT, GE, LT, LE,
    IN, NIN, BETWEEN,
    LIKE, ILIKE, STARTS_WITH, ENDS_WITH, CONTAINS,
    IS_NULL, NOT_NULL, EMPTY, NOT_EMPTY, EXISTS
}
