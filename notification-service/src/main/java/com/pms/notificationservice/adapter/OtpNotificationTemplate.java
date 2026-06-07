package com.pms.notificationservice.adapter;

import com.pms.notificationservice.dto.NotificationRequest;
import com.pms.notificationservice.model.NotificationChannel;
import com.pms.notificationservice.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OtpNotificationTemplate extends NotificationTemplate {

    private static final String PHONE_PATTERN = "[6-9]\\d{9}";

    public OtpNotificationTemplate(NotificationRepository notificationRepository,
                                   List<NotificationProvider> providers) {
        super(notificationRepository, providers);
    }

    @Override
    protected void validateRecipient(NotificationRequest request) {
        if (request.recipient() == null || request.recipient().isBlank()) {
            throw new IllegalArgumentException("Recipient cannot be empty for OTP");
        }
        if (request.channel() != NotificationChannel.SMS) {
            throw new IllegalArgumentException("OTP must be sent via SMS, not " + request.channel());
        }
        String digits = request.recipient().replaceAll("[^0-9]", "");
        if (digits.length() != 10 || !digits.matches(PHONE_PATTERN)) {
            throw new IllegalArgumentException("Invalid phone number for OTP: " + request.recipient());
        }
    }
}
