package com.hanguyen.notification.constant;

import lombok.Getter;

@Getter
public enum TypeEvent {
    CREATE("Create"),
    UPDATE("Update"),
    DELETE("Delete"),
    OAUTH2("OAuth2"),
    ;

    TypeEvent(String event) {
        this.event = event;
    }

    private final String event;
}
