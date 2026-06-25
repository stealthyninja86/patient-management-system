package com.pms.notificationservice.service.mapper;

import com.pms.notificationservice.dto.request.NotificationRequest;
import com.pms.notificationservice.dto.response.NotificationResponseDTO;
import com.pms.notificationservice.model.Notification;
import com.pms.notificationservice.model.NotificationStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class NotificationMapper {

    public Notification createNotification(NotificationRequest request) {
        return Notification.builder()
                .patientId(request.patientId())
                .type(request.type())
                .channel(request.channel())
                .recipient(request.recipient())
                .message(request.message())
                .status(NotificationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public NotificationResponseDTO toResponse(Notification notification) {
        return new NotificationResponseDTO(
            notification.getId(),
            notification.getPatientId(),
            notification.getType(),
            notification.getChannel(),
            notification.getRecipient(),
            notification.getMessage(),
            notification.getStatus(),
            notification.getRetryCount(),
            notification.getErrorMessage(),
            notification.getCreatedAt(),
            notification.getSentAt()
        );
    }
}
