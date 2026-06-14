package com.pms.notificationservice.service.factory;

import com.pms.notificationservice.dto.request.NotificationRequest;
import com.pms.notificationservice.model.Notification;
import com.pms.notificationservice.model.NotificationStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class NotificationFactory {

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
}
