package com.redhat.demo.resource;

import java.util.List;

import org.apache.camel.ProducerTemplate;

import com.redhat.demo.camel.saga.repository.OrderRepository;
import com.redhat.demo.entity.Order;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Path("/orders")
public class OrderResource {

    @Inject
    OrderRepository orderRepository;

    @Inject
    ProducerTemplate producerTemplate;

    @GET
    public List<Order> getAllOrders() {
        return orderRepository.listAll();
    }

    @GET
    @Path("/{id}")
    public Order getOrder(@PathParam("id") Long id) {
        return orderRepository.findById(id);
    }

}