package com.marcusprado02.sharedkernel.application.service.example.impl;

import com.marcusprado02.sharedkernel.infrastructure.payments.model.Customer;

/**
 * Porta de leitura para Customer.
 * Implementações podem ser mock (teste), JPA, REST client, etc.
 */
public interface CustomerQueryService {

    /**
     * Obtém um Customer pelo ID.
     * Lança IllegalArgumentException se não encontrado.
     */
    Customer get(String id);
}
