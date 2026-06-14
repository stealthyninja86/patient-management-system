package com.pms.scheduleservice.kafka;

import java.time.Instant;
import java.util.UUID;

public abstract class BaseEvent {

    private UUID eventId;
    private Instant occurredAt;
    private String version;

    public BaseEvent() {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.version = "1.0";
    }

    public abstract String getEventType();

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
