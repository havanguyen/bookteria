package com.hanguyen.notification.constant;

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
