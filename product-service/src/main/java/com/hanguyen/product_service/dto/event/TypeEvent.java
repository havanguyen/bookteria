package com.hanguyen.product_service.dto.event;

import lombok.Getter;

@Getter
public enum TypeEvent {
    CREATE("Create"),
    UPDATE("Update"),
    DELETE("Delete"),
    ;

    TypeEvent(String event) {
        this.event = event;
    }

    private final String event;
}
