package com.redhat.demo.camel.saga.route;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.SagaPropagation;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.saga.InMemorySagaService;

import com.redhat.demo.camel.saga.model.OrderDto;
import com.redhat.demo.camel.saga.service.PaymentService;
import com.redhat.demo.repository.PaymentRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PaymentRoute extends RouteBuilder {

        @Inject
        PaymentService paymentService;

        @Inject
        PaymentRepository paymentRepository;

        @Override
        public void configure() throws Exception {

                // Se agrega SAGA al contexto de Camel
                getContext().addService(new InMemorySagaService());

                onException(Exception.class)
                        .log("Exception occurred: ${exception.message}")
                        .handled(true)
                        .to("direct:cancelPayment");

                // Escuchar eventos de pedidos
                from("kafka:seat-events")
                        .saga()
                        .log("DEBUG - Received Kafka message: ${body}")
                        .unmarshal().json(JsonLibrary.Jackson, OrderDto.class)
                        .setHeader("price", simple("${body.price}"))
                        .setHeader("orderId", simple("${body.orderId}"))
                        .filter().simple("${body.seatStatus} == 'RESERVED' && ${body.orderStatus} == 'PENDING'")
                        .to("direct:processPayment");

                // Process payment
                from("direct:processPayment")
                        .saga().propagation(SagaPropagation.MANDATORY)
                        .bean(paymentService, "createPayment")
                        .setHeader("paymentId", simple("${body.paymentId}"))
                        .log("Header payment id: ${header.paymentId}")
                        .setBody(simple("${body}"))
                        .log("Persisting payment in database: ${body}")
                        // Database insert
                        .to("direct:insertPayment")
                        .marshal().json()
                        // Force an exception
                        //.process(exchange -> {
                        //        throw new RuntimeException("Error lanzado desde el Processor");
                        //})
                        .to("kafka:payment-events") // Publicar evento de pago exitoso
                        .log("Sending payment event to Kafka: ${body}")
                        .log("Payment sent to Kafka topic: payment-events");                        

                // Compensation
                from("direct:cancelPayment")
                        .log("COMPENSATION --> Cancelling payment for order: ${header.id}")
                        .bean(paymentService, "cancelPayment")
                        .marshal().json()
                        .log("Sending compensation event to Kafka: ${body}")
                        .to("kafka:compensation-events");
                
                from("direct:insertPayment")
                        .setHeader("paymentId", simple("${header.paymentId}"))
                        .setHeader("price", simple("${header.price}"))
                        //.setHeader("status", simple("${body.paymentStatus}"))
                        .setHeader("date", simple("${body.date}"))
                        .setHeader("status", constant("COMPLETED"))
                        .setHeader("orderMessage", constant("Payment completed."))
                        .log("Headers antes de insertar en SQL: paymentId=${header.paymentId}, price=${header.price}, status=${header.status}, date=${header.date}")
                        .log("SQL: {{sql.insertPayment}}")
                        .to("sql:{{sql.insertPayment}}")
                        .log("Payment created successfully");

                from("kafka:compensation-events")
                        .log("Compensation event from Kafka: ${body}")
                        .unmarshal().json(OrderDto.class)
                        .setHeader("paymentId", simple("${header.paymentId}"))
                        .setHeader("orderId", simple("${body.orderId}"))
                        .choice()
                                .when().simple("${body.seatStatus} == 'FAILED' && ${body.orderStatus} == 'PENDING'")
                                        .log("Payment compensation")
                                        .bean(paymentService, "paymentCompensation")
                                        .marshal().json()
                                        .log("Sending payment (compensated) event to Kafka: ${body}")
                                        .to("kafka:payment-events")
                                .when().simple("${body.paymentId} != null && ${body.seatStatus} == 'FAILED'")
                                        .log("Cancel payment")
                                        .bean(paymentService, "cancelPayment")
                                        .setHeader("status", constant("FAILED"))
                                        .to("direct:updatePayment")
                                .otherwise()
                                        .log("Payment was not persisted, nothing to do.")
                        ;
                        
                from("direct:updatePayment")
                        .log("Headers antes de actualizar en SQL: paymentId=${header.paymentId}, status=${header.status}")
                        .log("SQL: {{sql.updatePayment}}")
                        .to("sql:{{sql.updatePayment}}")
                        .log("Payment updated for orderId: ${header.orderId} with paymentId: ${header.paymentId}");
        }
}