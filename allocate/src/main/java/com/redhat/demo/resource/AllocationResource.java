package com.redhat.demo.resource;

import java.util.List;

import com.redhat.demo.entity.Seat;
import com.redhat.demo.repository.SeatRepository;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Path("/seats")
public class AllocationResource {

    @Inject
    SeatRepository seatRepository;

    @GET
    public List<Seat> getAllSeats() {
        return seatRepository.listAll();
    }

    @Path("/{seatId}")
    @GET
    public Seat getSeatBySeatId(@PathParam("seatId") String seatId) {
        return seatRepository.findBySeatId(seatId);
    }
    
}
