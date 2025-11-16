package com.hanguyen.order_service.dto;

import lombok.Getter;

@Getter
public enum TypeMessage {
    InventoryErrorRollBack("Inventory rollback failed due to an internal error."),
    InventoryOutOfStockReply("Inventory service reports that the product is out of stock."),
    OrderReserverReply("Inventory reservation result returned to the order service."),
    OrderRollBackReply("Order has been rolled back due to a failure in the workflow."),

    InitiatePaymentCommand("Request to initiate payment for the order."),
    ReserveInventoryCommand("Request to reserve inventory for the order."),
    RollbackInventoryCommand("Request to rollback inventory reservation."),

    CheckOrderTimeoutEvent("Triggered when checking whether the order has timed out."),
    OrderCompletedEvent("Order has been successfully completed."),
    PaymentFailedEvent("Payment failed during processing."),
    PaymentSucceededEvent("Payment was successfully processed."),
    PaymentInitiatedReply("Payment initiate reply")
    ;

    TypeMessage(String event) {
        this.message = event;
    }
    private final String message;
}

