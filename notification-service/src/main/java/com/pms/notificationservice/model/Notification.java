package com.pms.notificationservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String patientId;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;

    private String recipient;

    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    private int retryCount;

    private String errorMessage;

    private LocalDateTime createdAt;

    private LocalDateTime sentAt;

    public Notification() {}

    public Notification(UUID id, String patientId, NotificationType type, NotificationChannel channel,
                        String recipient, String message, NotificationStatus status, int retryCount,
                        String errorMessage, LocalDateTime createdAt, LocalDateTime sentAt) {
        this.id = id;
        this.patientId = patientId;
        this.type = type;
        this.channel = channel;
        this.recipient = recipient;
        this.message = message;
        this.status = status;
        this.retryCount = retryCount;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.sentAt = sentAt;
    }

    public static NotificationBuilder builder() {
        return new NotificationBuilder();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public NotificationChannel getChannel() { return channel; }
    public void setChannel(NotificationChannel channel) { this.channel = channel; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public NotificationStatus getStatus() { return status; }
    public void setStatus(NotificationStatus status) { this.status = status; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public static class NotificationBuilder {

        private UUID id;
        private String patientId;
        private NotificationType type;
        private NotificationChannel channel;
        private String recipient;
        private String message;
        private NotificationStatus status;
        private int retryCount;
        private String errorMessage;
        private LocalDateTime createdAt;
        private LocalDateTime sentAt;

        NotificationBuilder() {}

        public NotificationBuilder id(UUID id) { this.id = id; return this; }
        public NotificationBuilder patientId(String patientId) { this.patientId = patientId; return this; }
        public NotificationBuilder type(NotificationType type) { this.type = type; return this; }
        public NotificationBuilder channel(NotificationChannel channel) { this.channel = channel; return this; }
        public NotificationBuilder recipient(String recipient) { this.recipient = recipient; return this; }
        public NotificationBuilder message(String message) { this.message = message; return this; }
        public NotificationBuilder status(NotificationStatus status) { this.status = status; return this; }
        public NotificationBuilder retryCount(int retryCount) { this.retryCount = retryCount; return this; }
        public NotificationBuilder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
        public NotificationBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public NotificationBuilder sentAt(LocalDateTime sentAt) { this.sentAt = sentAt; return this; }

        public Notification build() {
            return new Notification(
                    id, patientId, type, channel, recipient, message,
                    status, retryCount, errorMessage, createdAt, sentAt
            );
        }
    }
}
