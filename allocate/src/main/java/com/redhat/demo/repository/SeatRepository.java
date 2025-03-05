package com.redhat.demo.repository;

import java.util.List;

import com.redhat.demo.entity.Seat;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SeatRepository implements PanacheRepository<Seat> {
    
    public Seat findById(Long id) {
        return find("id", id).firstResult();
    }

    public Seat findBySeatId(String seatId) {
        return find("seatId", seatId).firstResult();
    }

    public List<Seat> findAllSeats() {
        return listAll();
    }

}