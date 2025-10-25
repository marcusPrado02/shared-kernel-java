package com.marcusprado02.sharedkernel.infrastructure.sms.api;

import java.util.Locale;
import java.util.Map;

import com.marcusprado02.sharedkernel.infrastructure.sms.model.SmsRequest;

public interface TemplateRenderer {
    /** Renderiza template (ex. Mustache/Handlebars) com fallback i18n e aplica normalização. */
    String render(String templateId, Map<String, Object> params, Locale locale, int maxLen);
    default String render(SmsRequest req, int maxLen) {
        return render(req.templateId(), req.params(), req.locale(), req.maxLength());
    }
}