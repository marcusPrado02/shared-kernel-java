package com.marcusprado02.sharedkernel.infrastructure.email.api;

import java.util.Locale;
import java.util.Map;

import com.marcusprado02.sharedkernel.infrastructure.email.model.EmailRequest;

public interface TemplateRenderer {
    String renderText(String templateId, Map<String,Object> params, Locale locale);
    String renderHtml(String templateId, Map<String,Object> params, Locale locale);
    default EmailRequest render(EmailRequest req){
        if (req.text() != null || req.html() != null) return req;
        String text = renderText(req.templateId(), req.params(), req.locale());
        String html = renderHtml(req.templateId(), req.params(), req.locale());
        return req.withRendered(text, html);
    }
}
