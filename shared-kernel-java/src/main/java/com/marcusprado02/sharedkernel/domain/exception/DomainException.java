package com.marcusprado02.sharedkernel.domain.exception;


import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.ToString;

/**
 * Exceção de domínio rica e padronizada. Ideal para propagar erros funcionais e semânticos de forma
 * estruturada.
 */
@Getter
@ToString(callSuper = true)
public abstract class DomainException extends RuntimeException implements Serializable {

    /** Código único do erro (ex: ORDER_NOT_FOUND, USER_BLOCKED) */
    private final String errorCode;

    /** Parâmetros de contexto (ex: orderId, userId, amount) */
    private final Map<String, Object> parameters;

    /** Timestamp de criação da exceção */
    private final Instant occurredOn;

    /** Versão do contrato da exceção (útil para versionamento de APIs) */
    private final int version = 1;

    /**
     * Construtor com código e mensagem simples.
     */
    protected DomainException(String errorCode, String message) {
        super(message);
        this.errorCode = Objects.requireNonNull(errorCode);
        this.parameters = Collections.emptyMap();
        this.occurredOn = Instant.now();
    }

    /**
     * Construtor com código, mensagem e parâmetros de contexto.
     */
    protected DomainException(String errorCode, String message, Map<String, Object> parameters) {
        super(message);
        this.errorCode = Objects.requireNonNull(errorCode);
        this.parameters = parameters == null ? Collections.emptyMap()
                : Collections.unmodifiableMap(parameters);
        this.occurredOn = Instant.now();
    }
}
