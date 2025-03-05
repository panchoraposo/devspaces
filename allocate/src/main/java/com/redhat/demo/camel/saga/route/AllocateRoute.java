package com.redhat.demo.camel.saga.route;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.SagaPropagation;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.saga.InMemorySagaService;

import com.redhat.demo.camel.saga.model.OrderDto;
import com.redhat.demo.camel.saga.service.AllocateService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AllocateRoute extends RouteBuilder {

        @Inject
        AllocateService allocateService;

        @Override
        public void configure() throws Exception {

                getContext().addService(new InMemorySagaService());

                onException(Exception.class)
                        .log("Exception occurred: ${exception.message}")
                        .handled(true)
                        .to("direct:compensateAllocation");

                // Consumir eventos de ordenes
                from("kafka:order-events")
                        .unmarshal().json(JsonLibrary.Jackson, OrderDto.class)
                        .setHeader("seatId", simple("${body.seatId}"))
                        .saga()
                        .log("DEBUG - Received Kafka message: ${body}")
                        .setBody(simple("${body}"))
                        .setHeader("seatId", simple("${body.seatId}"))
                        .setHeader("orderId", simple("${body.orderId}"))
                        .to("direct:allocateSeat");
                
                // Asignar asiento, y si falla, realizar compensación
                from("direct:allocateSeat")
                        .saga()
                        .propagation(SagaPropagation.MANDATORY)
                        .log("Allocating seat for: ${body}")
                        .bean(allocateService, "allocateSeat")
                        // Actualizar el estado del asiento en base de datos
                        .log("Updating seat in database: ${body}")
                        .to("direct:updateSeat")
                        // Producir evento a Kafka
                        .marshal().json(JsonLibrary.Jackson)
                        .log("Sending allocation event to Kafka: ${body}")
                        .to("kafka:seat-events")
                        .log("Seat allocation sent to Kafka topic: seat-events");

                // Compensar la asignación en caso de fallo
                from("direct:compensateAllocation")
                        .log("Compensation triggered for seat: ${header.seatId} in order: ${header.orderId}")
                        .bean(allocateService, "revertAllocation")
                        .marshal().json(JsonLibrary.Jackson)
                        .log("Sending compensation event to Kafka topic compensation-events: ${body}")
                        .to("kafka:compensation-events");
                
                // Update seat
                from("direct:updateSeat")
                        .setHeader("seatId", simple("${header.seatId}"))
                        .setHeader("newStatus", simple("${body.seatStatus}"))
                        .log("Headers antes de actualizar en SQL: seatId=${header.seatId}, status=${header.newStatus}")
                        .log("SQL: {{sql.updateSeat}}")
                        .to("sql:{{sql.updateSeat}}")
                        .log("Seat allocated successfully");

        }
}