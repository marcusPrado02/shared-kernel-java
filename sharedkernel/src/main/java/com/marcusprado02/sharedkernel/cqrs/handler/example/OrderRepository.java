package com.marcusprado02.sharedkernel.cqrs.handler.example;

import java.util.List;

import com.marcusprado02.sharedkernel.domain.model.example.entity.Order;

public interface OrderRepository {
    void save(Order order);
    boolean exists(String orderId);
    Order findById(String orderId);
    List<Object> pullDomainEventsAndClear(Order order); // domain events mantidos no aggregate
}
