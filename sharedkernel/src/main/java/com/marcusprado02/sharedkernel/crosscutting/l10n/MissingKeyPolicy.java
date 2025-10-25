package com.marcusprado02.sharedkernel.crosscutting.l10n;

public enum MissingKeyPolicy {
    RETURN_KEY,     // retorna "order.created.title"
    RETURN_MARKED,  // retorna "??order.created.title??"
    THROW           // lança MissingMessageException
}