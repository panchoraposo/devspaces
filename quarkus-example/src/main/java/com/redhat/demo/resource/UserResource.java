package com.redhat.demo.resource;

import java.util.List;

import com.redhat.demo.repository.UserRepository;
import com.redhat.demo.entity.User;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/users")
public class UserResource {

    @Inject
    UserRepository userRepository;

    @GET
    public List<User> getAllUsers() {
        return userRepository.listAll();
    }
    
}