package com.redhat.demo.resource;

import java.util.List;

import com.redhat.demo.entity.Payment;
import com.redhat.demo.repository.PaymentRepository;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Path("/payments")
public class PaymentResource {

    @Inject
    PaymentRepository paymentRepository;

    @GET
    public List<Payment> getAllPayments() {
        return paymentRepository.listAll();
    }

    @Path("/{paymentId}")
    @GET
    public Payment findByPaymentId(@PathParam("paymentId") String paymentId) {
        return paymentRepository.findPaymentById(paymentId);
    }

}