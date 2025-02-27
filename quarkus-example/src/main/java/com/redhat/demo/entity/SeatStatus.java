package com.redhat.demo.entity;

public enum SeatStatus {
    
    FREE("free"),
    BLOCKED("not available"),
    RESERVED("reserved");

    private final String description;

    SeatStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}