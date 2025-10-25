package com.marcusprado02.sharedkernel.infrastructure.payments.recon;

import java.time.LocalDate;
import java.util.List;

public interface ReconciliationService {
    /** Baixa o extrato do provider e reconcilia com o ledger interno. */
    List<ReconciliationResult> reconcile(String providerId, LocalDate from, LocalDate to);
}
