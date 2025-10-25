package com.marcusprado02.sharedkernel.domain.service.error;


import java.io.Serializable;

public sealed interface DomainException extends Serializable
        permits ValidationException, BusinessRuleException, ConcurrencyException  {

    String code();      // ex.: "PRICING.INVALID_PROMO"
    String message();   // humano
}
