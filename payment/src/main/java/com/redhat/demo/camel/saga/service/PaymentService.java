package com.redhat.demo.camel.saga.service;

import java.time.Instant;

import org.apache.camel.Exchange;

import com.redhat.demo.camel.saga.model.OrderDto;
import com.redhat.demo.entity.Payment;
import com.redhat.demo.repository.PaymentRepository;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;

@ApplicationScoped
@Named("paymentService")
public class PaymentService {

    @Inject
    PaymentRepository paymentRepository;

    public OrderDto createPayment(OrderDto order) {
        String orderId = order.getOrderId();
        // Crear un ID de pago solo con el orderId
        String paymentId = "PAY-" + orderId;
        order.setPaymentStatus("COMPLETED");
        order.setPaymentId(paymentId);
        order.setDate(Instant.now().toEpochMilli());
        order.setPrice(order.getPrice());
        order.setUserId(order.getUserId());
        order.setOrderId(order.getOrderId());
        order.setSeatId(order.getSeatId());
        order.setOrderStatus(order.getOrderStatus());
        order.setPaymentMessage("Payment successful");
        
        return order;

    }

    @Transactional
    public void cancelPayment(Exchange exchange) {
        
        String paymentId = exchange.getMessage().getHeader("paymentId", String.class);
                        
        Payment payment = paymentRepository.findPaymentById(paymentId);
        if (payment == null) {
            Log.info("Payment does not exist. Nothing to do.");
            exchange.getMessage().setHeader("paymentStatus", "CANCELLED");
            exchange.getMessage().setHeader("paymentMessage", "Payment does not exist. Nothing to do.");
        }
        return;
    }

    public OrderDto paymentCompensation(Exchange exchange) {
        OrderDto order = exchange.getMessage().getBody(OrderDto.class);
        order.setPaymentStatus("CANCELLED");
        order.setPaymentMessage("Compensation for payment due to an allocation error.");
        order.setDate(Instant.now().toEpochMilli());
        return order;
    }

}