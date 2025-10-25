package com.marcusprado02.sharedkernel.infrastructure.payments.model;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public record Customer(
        String customerId, String email, String name, String phone,
        Address billingAddress, Address shippingAddress
) {}
