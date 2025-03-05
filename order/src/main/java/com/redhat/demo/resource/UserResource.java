package com.redhat.demo.resource;

import java.util.List;

import com.redhat.demo.camel.saga.repository.UserRepository;
import com.redhat.demo.entity.User;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/users")
public class UserResource {

    @Inject
    MeterRegistry registry;

    @Inject
    UserRepository userRepository;

    @GET
    @WithSpan(value = "Not needed, will change the current span name")
    public List<User> getAllUsers() {
        registry.counter("getusers_counter", Tags.of("user", "all")).increment();
        return userRepository.listAll();
    }
    
}