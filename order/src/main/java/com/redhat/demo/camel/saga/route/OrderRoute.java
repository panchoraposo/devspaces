package com.redhat.demo.camel.saga.route;

import java.sql.SQLException;
import java.util.UUID;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.SagaPropagation;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.saga.InMemorySagaService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.redhat.demo.camel.saga.model.OrderDto;
import com.redhat.demo.camel.saga.repository.OrderRepository;
import com.redhat.demo.camel.saga.service.OrderService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class OrderRoute extends RouteBuilder {

        @Inject
        OrderService orderService;

        @Inject
        OrderRepository orderRepository;

        @Override
        public void configure() throws Exception {

                restConfiguration()
                        .component("platform-http")
                        .bindingMode(RestBindingMode.auto);

                // Se agrega SAGA al contexto de Camel
                getContext().addService(new InMemorySagaService());

                onException(SQLException.class)
                        .log("Exception occurred: ${exception.message}")
                        .handled(true)
                        .to("direct:cancelOrder");

                // Endpoint REST para crear un pedido
                from("rest:post:/order")
                        .unmarshal().json(JsonLibrary.Jackson, OrderDto.class)
                        .process(exchange -> {
                                exchange.getMessage().setHeader("id", UUID.randomUUID().toString());
                                OrderDto order = exchange.getMessage().getBody(OrderDto.class);
                                order.setOrderId(exchange.getMessage().getHeader("id", String.class));
                                exchange.getMessage().setBody(order);

                                ObjectMapper objectMapper = new ObjectMapper();
                                objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
                                String jsonOrder = objectMapper.writeValueAsString(order);
                                exchange.getMessage().setHeader("orderJson", jsonOrder);
                        })
                        .log(LoggingLevel.INFO, "Order received: ${header.orderJson}")
                        // SAGA is started
                        .saga()
                        .to("direct:newOrder")
                        .marshal().json(JsonLibrary.Jackson);

                from("direct:newOrder")
                        .log("Processing order id: ${header.id}")
                        .saga().propagation(SagaPropagation.MANDATORY)
                        .option("id", header("id"))
                        .setBody(body())
                        .bean(orderService, "createOrder")
                        .log("Persisting order in database: ${header.id}")
                        // Database
                        .to("direct:insertOrder")
                        .process(exchange -> {
                                OrderDto order = exchange.getIn().getBody(OrderDto.class);
                                ObjectMapper objectMapper = new ObjectMapper();
                                String json = objectMapper.writeValueAsString(order);
                                exchange.getIn().setBody(json);
                        })
                        .log("Sending order event to Kafka: ${body}")
                        .to("kafka:order-events")
                        .log("Order sent to Kafka topic: order-events.");
                
                from("direct:insertOrder")
                        .setHeader("orderId", simple("${body.orderId}"))
                        .setHeader("seatId", simple("${body.seatId}"))
                        .setHeader("orderStatus", simple("${body.orderStatus}"))
                        .setHeader("userId", simple("${body.userId}"))
                        .setHeader("orderMessage", simple("${body.orderMessage}"))
                        .log("Headers: orderId=${header.orderId}, userId=${header.userId}, seatId=${header.seatId}, orderStatus=${header.orderStatus}, orderMessage=${header.orderMessage}")
                        .log("SQL: {{sql.insertOrder}}")
                        .to("sql:{{sql.insertOrder}}")
                        .log("Order inserted successfully");

                // Compensation
                from("direct:cancelOrder")
                        .log("COMPENSATION -> Cancelling order: ${header.id}")
                        .bean(orderService, "cancelOrder")
                        .setHeader("orderStatus", constant("CANCELLED"))
                        .setHeader("orderMessage", constant("Order failed to create."));

                from("kafka:compensation-events")
                        .unmarshal().json(OrderDto.class)
                        .log("Compensation event from Kafka: ${body}")
                        .to("direct:cancelOrder");

                from("kafka:payment-events")
                        .log("Raw Payment event from Kafka: ${body}")
                        .unmarshal().json(JsonLibrary.Jackson, OrderDto.class)
                        .log("Parsed OrderDto: ${body}")
                        .log("Order Status: ${body.orderStatus}, Order Message: ${body.orderMessage}")
                        .setHeader("orderId", simple("${body.orderId}"))
                        .choice()
                                .when().simple("${body.seatStatus} == 'FAILED' || ${body.paymentStatus} == 'CANCELLED'")
                                        .setHeader("orderStatus", constant("FAILED"))
                                        .setHeader("orderMessage", constant("Order failed to complete."))
                                .when().simple("${body.seatStatus} == 'RESERVED' || ${body.paymentStatus} == 'COMPLETED'")
                                        .setHeader("orderStatus", constant("COMPLETED"))
                                        .setHeader("orderMessage", constant("Order completed."))
                        .end()
                        .setHeader("paymentId", simple("${body.paymentId}"))
                        .setHeader("paymentStatus", simple("${body.paymentStatus}"))
                        .setHeader("paymentMessage", simple("${body.paymentMessage}"))
                        .setHeader("seatStatus", simple("${body.seatStatus}"))
                        .setHeader("seatMessage", simple("${body.seatMessage}"))
                        .setHeader("date", simple("${body.date}"))
                        .setHeader("price", simple("${body.price}"))
                        .to("direct:updateOrder");

                from("direct:updateOrder")
                        .log("Headers antes de actualizar en SQL: orderId=${header.orderId}, paymentId=${header.paymentId}, paymentStatus=${header.paymentStatus}, paymentMessage=${header.paymentMessage}, seatStatus=${header.seatStatus}, seatMessage=${header.seatMessage}, orderStatus=${header.orderStatus}, orderMessage=${header.orderMessage}")
                        .bean(orderService, "updateOrder")
                        .log("SQL: {{sql.updateOrder}}")
                        .to("sql:{{sql.updateOrder}}")
                        .log("Order: ${header.orderId} updated.")
                        .choice()
                                .when().simple("${body.seatStatus} == 'RESERVED' || ${body.paymentStatus} == 'COMPLETED'")
                                        .bean(orderService, "updateUserBudget")
                                
                        ;

        }

}