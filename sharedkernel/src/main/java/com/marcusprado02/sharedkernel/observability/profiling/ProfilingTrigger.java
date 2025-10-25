package com.marcusprado02.sharedkernel.observability.profiling;

import java.time.*;
import java.util.*;

public interface ProfilingTrigger {
    EvaluationResult evaluate(ProfilingContext ctx);

    default String name() { return getClass().getSimpleName(); }
}
