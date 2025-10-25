package com.marcusprado02.sharedkernel.application.port.example.impl;

import java.util.Optional;

/** Porta de leitura para enriquecer documentos com nome do cliente. */
public interface CustomerPort {
    Optional<String> findName(String customerId);
}