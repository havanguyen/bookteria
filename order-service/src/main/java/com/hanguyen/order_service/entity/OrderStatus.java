package com.hanguyen.order_service.entity;


import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING("Order is pending and awaiting processing"),
    PAID("Order has been successfully paid"),
    SHIPPED("Order has been shipped to the customer"),
    COMPLETED("Order has been delivered and completed"),
    CANCELLED("Order has been cancelled");

    private final String message;

    OrderStatus(String message) {
        this.message = message;
    }

}
