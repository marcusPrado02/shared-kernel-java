package com.marcusprado02.sharedkernel.infrastructure.email.model;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public record EmailRequest(
        String idempotencyKey,
        Address from, List<Address> to, List<Address> cc, List<Address> bcc, Address replyTo,
        String subject,
        String text, String html, // ou usar template
        String templateId, Map<String,Object> params, Locale locale,
        List<Attachment> attachments, Headers headers,
        EmailContext context, Priority priority, TrackOptions trackOptions,
        Map<String,Object> metadata, boolean requireDeliveryReport
) {
    public EmailRequest withRendered(String text, String html) {
        return new EmailRequest(idempotencyKey, from, to, cc, bcc, replyTo, subject, text, html,
                templateId, params, locale, attachments, headers, context, priority, trackOptions, metadata, requireDeliveryReport);
    }
}
