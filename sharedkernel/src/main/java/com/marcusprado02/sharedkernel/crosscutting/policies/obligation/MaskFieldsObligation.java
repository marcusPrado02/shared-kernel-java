package com.marcusprado02.sharedkernel.crosscutting.policies.obligation;

import java.util.List;
import java.util.Map;

import com.marcusprado02.sharedkernel.crosscutting.redaction.RedactionUtil;

public final class MaskFieldsObligation implements ObligationApplier {
    @Override
    public Object apply(Object response, Map<String, Object> obligations) {
        var fields = (List<String>) obligations.getOrDefault("maskFields", List.of());
        return RedactionUtil.maskFields(response, fields); // implementar via Jackson MixIn/BeanWrapper
    }
}