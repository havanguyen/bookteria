package com.hanguyen.identity.constant;

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
