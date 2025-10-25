package com.marcusprado02.sharedkernel.infrastructure.email.model;

import java.util.List;

public record InboundEmail(
        Address from, List<Address> to, Address replyTo,
        String subject, String text, String html, List<Attachment> attachments, Headers headers
) {}
