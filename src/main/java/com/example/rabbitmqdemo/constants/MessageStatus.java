package com.example.rabbitmqdemo.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MessageStatus {
    SENT("sent"), // Mensagem enviada
    CONSUMED("consumed"); // Mensagem recebida

    private String description;

    MessageStatus(String description) {
        this.description = description;
    }

    @JsonValue
    public String getDescription() {
        return description;
    }

    @JsonCreator
    public static MessageStatus fromDescription(String description) {
        for (MessageStatus status : MessageStatus.values()) {
            if (status.description.equalsIgnoreCase(description)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + description);
    }
}