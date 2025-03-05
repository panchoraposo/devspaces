package com.redhat.demo.camel.saga.repository;

import com.redhat.demo.entity.Order;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OrderRepository implements PanacheRepository<Order> {

    public Order findById(Long id) {
        return find("id", id).firstResult();
    }
    
    public Order findByOrderId(String orderId) {
        return find("orderId", orderId).firstResult();
    }

}