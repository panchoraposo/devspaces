package com.redhat.demo.entity;

public enum PaymentStatus {
    
    COMPLETED("completed"),
    FAILED("failed"),
    PENDING("pending"),
    REFUNDED("refunded");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}