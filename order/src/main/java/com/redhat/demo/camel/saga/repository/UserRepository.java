package com.redhat.demo.camel.saga.repository;

import java.util.List;

import com.redhat.demo.entity.User;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    public User findById(Long id) {
        return find("id", id).firstResult();
    }

    public List<User> findAllUsers() {
        return listAll();
    }
    
}