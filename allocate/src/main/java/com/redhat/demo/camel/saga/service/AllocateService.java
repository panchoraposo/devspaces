package com.redhat.demo.camel.saga.service;

import org.jboss.logging.Logger;

import com.redhat.demo.camel.saga.model.OrderDto;
import com.redhat.demo.entity.Seat;
import com.redhat.demo.repository.SeatRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;

@ApplicationScoped
@Named("allocateService")
public class AllocateService {

    @Inject
    SeatRepository seatRepository;

    private static final Logger LOG = Logger.getLogger(AllocateService.class);

    @Transactional
    public OrderDto allocateSeat(OrderDto order) {
        
        LOG.info("Veryfing seat allocation for order: " + order.getOrderId() + " and seat: " + order.getSeatId());
        String seatId = order.getSeatId();
        // Verificamos si el asiento ya está ocupado
        Seat seat = seatRepository.findBySeatId(seatId);
        if (seat != null && !seat.getStatus().equalsIgnoreCase("FREE")) {
            LOG.error("Seat " + seatId + " is already allocated.");
            throw new RuntimeException("Seat " + seatId + " is already allocated.");
        }
        // Lógica para asignar el asiento
        order.setSeatStatus("RESERVED");
        order.setSeatMessage("Seat: " + seatId + " reserved for Order: " + order.getOrderId());
        LOG.info("Seat " + seatId + " reserved.");
        return order;

    }

    public OrderDto revertAllocation(OrderDto order) {
        LOG.info("Reverting allocation for Order: " + order.getOrderId());
        order.setSeatStatus("FAILED");
        order.setSeatMessage("Seat " + order.getSeatId() + " is already allocated.");
        return order;
    }

    // Método auxiliar para verificar el estado de un asiento
    public boolean isSeatAllocated(String seatId) {
        Seat seat = seatRepository.findBySeatId(seatId);
        if (seat != null && !seat.getStatus().equalsIgnoreCase("FREE")) {
            return true;
        }        
        return false;
    }
    
}