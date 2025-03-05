package com.redhat.demo.repository;

import java.util.List;

import com.redhat.demo.entity.Payment;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PaymentRepository implements PanacheRepository<Payment> {
    
    public Payment findById(Long id) {
        return find("id", id).firstResult();
    }

    public List<Payment> findAllPayments() {
        return listAll();
    }

    public Payment findPaymentById(String paymentId) {
        return find("paymentId", paymentId).firstResult();
    }

}