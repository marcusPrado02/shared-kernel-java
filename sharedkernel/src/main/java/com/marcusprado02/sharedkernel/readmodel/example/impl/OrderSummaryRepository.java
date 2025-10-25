package com.marcusprado02.sharedkernel.readmodel.example.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OrderSummaryRepository extends JpaRepository<OrderSummaryView, Long>,
        JpaSpecificationExecutor<OrderSummaryView> {}

