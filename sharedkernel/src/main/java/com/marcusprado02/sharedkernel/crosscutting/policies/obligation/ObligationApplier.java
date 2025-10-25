package com.marcusprado02.sharedkernel.crosscutting.policies.obligation;

import java.util.Map;

public interface ObligationApplier {
    Object apply(Object response, Map<String,Object> obligations);
}
