package com.platform.domain;

import java.time.LocalDateTime;
import java.util.Objects;

/** An in-memory notification record sent to a recipient. */
public class Notification {

    private final String id;
    private final String recipientId;
    private final String message;
    private final LocalDateTime timestamp;

    public Notification(String id, String recipientId, String message, LocalDateTime timestamp) {
        this.id = Objects.requireNonNull(id, "id");
        this.recipientId = Objects.requireNonNull(recipientId, "recipientId");
        this.message = Objects.requireNonNull(message, "message");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
    }

    public String        getId()          { return id; }
    public String        getRecipientId() { return recipientId; }
    public String        getMessage()     { return message; }
    public LocalDateTime getTimestamp()   { return timestamp; }

    @Override
    public String toString() {
        return String.format("[NOTIFICATION] To=%s | %s | %s", recipientId, timestamp, message);
    }
}
