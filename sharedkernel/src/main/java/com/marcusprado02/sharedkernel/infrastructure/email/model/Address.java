package com.marcusprado02.sharedkernel.infrastructure.email.model;

public record Address(String name, String email) {
    public Address {
        if (email == null || !email.contains("@")) throw new IllegalArgumentException("email inválido");
    }
}
