package com.hanguyen.payment_service.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum PaymentStatus {

    PENDING("Payment is pending and awaiting processing"),
    SUCCESS("Payment has been completed successfully"),
    FAILED("Payment has failed or was declined");

    private final String message;

    PaymentStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
