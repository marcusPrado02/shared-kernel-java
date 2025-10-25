package com.marcusprado02.sharedkernel.saga.example.Impl;

import java.math.BigDecimal;
import java.util.Map;

import com.marcusprado02.sharedkernel.saga.SagaData;

public record OrderPlacementData(String orderId, Map<String,Integer> items, BigDecimal amount) implements SagaData {}

