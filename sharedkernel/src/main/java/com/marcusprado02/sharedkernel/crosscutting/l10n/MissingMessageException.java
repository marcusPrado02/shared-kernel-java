package com.marcusprado02.sharedkernel.crosscutting.l10n;

public class MissingMessageException extends RuntimeException {
    public MissingMessageException(String k){ super("Missing i18n message: " + k); }
}