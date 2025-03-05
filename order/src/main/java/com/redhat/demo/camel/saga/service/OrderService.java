package com.redhat.demo.camel.saga.service;

import java.time.temporal.ChronoUnit;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.jboss.logging.Logger;

import com.redhat.demo.camel.saga.model.OrderDto;
import com.redhat.demo.entity.User;

import io.quarkus.logging.Log;

import com.redhat.demo.camel.saga.repository.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;

@ApplicationScoped
@Named("orderService")
public class OrderService {

    private static final Logger LOG = Logger.getLogger(OrderService.class);

    @Inject
    ProducerTemplate producerTemplate;

    @Inject
    UserRepository userRepository;

    // Create order
    @Retry(maxRetries = 3, delay = 2, delayUnit = ChronoUnit.SECONDS)
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.75, successThreshold = 4)
    @Timeout(500)
    @Fallback(fallbackMethod = "orderFallback")
    public OrderDto createOrder(Exchange exchange) {
        String orderId = exchange.getMessage().getHeader("id", String.class);
        OrderDto order = exchange.getMessage().getBody(OrderDto.class);
        order.setOrderId(orderId);
        order.setOrderStatus("PENDING");
        order.setOrderMessage("Order " + order.getOrderId() + " created successfully.");
        return order;
    }

    public OrderDto orderFallback(Exchange exchange) {
        // Lógica en caso de que falle la creación de la orden
        OrderDto order = new OrderDto();
        String orderId = exchange.getMessage().getHeader("id", String.class);
        order.setOrderId(orderId);
        order.setOrderStatus("FAILED");
        order.setOrderMessage("Order could not be created.");
        return order;
    }

    public OrderDto updateOrder(OrderDto order) {
        if (order.getPaymentStatus() != null && order.getPaymentStatus().equalsIgnoreCase("COMPLETED") && order.getSeatStatus() != null && order.getSeatStatus().equalsIgnoreCase("RESERVED")) {
            //order.setOrderStatus("COMPLETED");
            //order.setOrderMessage("Order: " + order.getOrderId() + " completed.");
            //updateUserBudget(order);
        }
        return order;
    }

    public void cancelOrder(Exchange exchange) {

        String orderId = exchange.getMessage().getHeader("id", String.class);
        LOG.info("Cancelling order " + orderId);
        OrderDto order = exchange.getMessage().getBody(OrderDto.class);

        if (order == null) {
            throw new IllegalArgumentException("OrderDto is null. Check your JSON payload.");
        }

        order.setOrderMessage("Order ${header.id} cancelled.");
        order.setOrderStatus("CANCELLED");

    }

    @Transactional
    public void updateUserBudget(OrderDto order) {
        if (null != order) {
            User user = userRepository.findById(order.getUserId());
            Double budget = user.getBudget();
            Double newBudget = budget - order.getPrice();
            user.setBudget(newBudget);
            Log.info("Updating budget for user: " + order.getUserId() + ". Before: " + budget + " . After: " + newBudget);
            userRepository.persist(user); 
        }

    }

}