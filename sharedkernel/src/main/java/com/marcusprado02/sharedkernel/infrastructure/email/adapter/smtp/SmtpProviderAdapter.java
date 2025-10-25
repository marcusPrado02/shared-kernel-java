package com.marcusprado02.sharedkernel.infrastructure.email.adapter.smtp;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.util.*;
import java.util.stream.Collectors;

import com.marcusprado02.sharedkernel.infrastructure.email.api.Capabilities;
import com.marcusprado02.sharedkernel.infrastructure.email.api.Policy;
import com.marcusprado02.sharedkernel.infrastructure.email.core.BaseProviderAdapter;
import com.marcusprado02.sharedkernel.infrastructure.email.model.*;
import com.marcusprado02.sharedkernel.infrastructure.email.model.Address;
import com.marcusprado02.sharedkernel.infrastructure.email.spi.ProviderConfig;
import com.marcusprado02.sharedkernel.infrastructure.email.spi.ProviderMetadata;
import com.marcusprado02.sharedkernel.infrastructure.email.spi.SignatureResolver;

public class SmtpProviderAdapter extends BaseProviderAdapter {

    private static final String ID = "smtp";
    private final ProviderConfig cfg;
    private final Session session;

    public SmtpProviderAdapter(ProviderConfig cfg) {
        this.cfg = cfg;
        Properties props = new Properties();
        props.put("mail.smtp.host", cfg.require("host"));
        props.put("mail.smtp.port", cfg.require("port"));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        this.session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(cfg.require("username"), cfg.require("password"));
            }
        });
    }

    @Override
    public ProviderMetadata metadata() {
        return new ProviderMetadata(
                ID,
                "SMTP Generic",
                "1.0",
                Set.of("GLOBAL"),
                new Capabilities(false, true, false, false, false)
        );
    }

    @Override
    public EmailResponse send(EmailRequest req, Policy policy) {
        return this.<EmailResponse>run(policy, () -> {
            try {
                MimeMessage msg = new MimeMessage(session);
                msg.setFrom(new InternetAddress(req.from().email(), req.from().name()));
                for (Address a : req.to()) {
                    msg.addRecipient(Message.RecipientType.TO, new InternetAddress(a.email(), a.name()));
                }
                if (req.cc() != null) {
                    for (Address a : req.cc()) {
                        msg.addRecipient(Message.RecipientType.CC, new InternetAddress(a.email(), a.name()));
                    }
                }
                if (req.bcc() != null) {
                    for (Address a : req.bcc()) {
                        msg.addRecipient(Message.RecipientType.BCC, new InternetAddress(a.email(), a.name()));
                    }
                }
                msg.setSubject(req.subject(), "UTF-8");

                Multipart multi = new MimeMultipart("mixed");
                if (req.html() != null) {
                    MimeBodyPart html = new MimeBodyPart();
                    html.setContent(req.html(), "text/html; charset=UTF-8");
                    multi.addBodyPart(html);
                }
                if (req.text() != null) {
                    MimeBodyPart txt = new MimeBodyPart();
                    txt.setText(req.text(), "UTF-8");
                    multi.addBodyPart(txt);
                }
                if (req.attachments() != null) {
                    for (Attachment att : req.attachments()) {
                        MimeBodyPart part = new MimeBodyPart();
                        part.setFileName(att.filename());
                        String ct = switch (att.mimeType()) {
                            case TEXT_HTML -> "text/html";
                            case TEXT_PLAIN -> "text/plain";
                            default -> "application/octet-stream";
                        };
                        part.setContent(att.data(), ct);
                        if (att.inline() && att.contentId() != null) {
                            part.setHeader("Content-ID", "<" + att.contentId() + ">");
                        }
                        multi.addBodyPart(part);
                    }
                }
                msg.setContent(multi);
                Transport.send(msg);

                String id = Optional.ofNullable(msg.getMessageID()).orElse("SMTP-" + System.currentTimeMillis());
                return ok(ID, id, Map.of("smtpHost", cfg.require("host")));
            } catch (Exception e) {
                return failed(ID, EmailErrorCode.UNKNOWN, Map.of("error", e.getMessage()));
            }
        });
    }

    @Override
    public List<EmailResponse> sendBulk(BulkRequest req, Policy policy) {
        return req.messages().stream().map(r -> send(r, policy)).collect(Collectors.toList());
    }

    @Override
    public EmailEvent getStatus(String messageId, EmailContext ctx) {
        return new EmailEvent(ID, messageId, EmailStatus.SENT, "sent", null, java.time.Instant.now(), Map.of());
    }

    @Override
    public WebhookResult handleWebhook(String body, String signatureHeader, SignatureResolver resolver) {
        // SMTP puro não possui webhooks nativos
        return new WebhookResult(false, "smtp-no-webhook");
    }

    @Override
    public InboundResult handleInbound(byte[] rawMime) {
        // Inbound via SMTP geralmente é entregue por outro canal (ex.: Mailgun/SendGrid Inbound)
        return new InboundResult(false, "smtp-inbound-external");
    }
}
