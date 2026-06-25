package com.pms.notificationservice.service.adapter;

import com.pms.notificationservice.dto.request.NotificationRequest;
import com.pms.notificationservice.model.NotificationChannel;
import com.pms.notificationservice.repository.NotificationRepository;
import com.pms.notificationservice.service.metrics.MetricsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class EmailNotificationTemplate extends NotificationTemplate {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public EmailNotificationTemplate(NotificationRepository notificationRepository,
                                      List<NotificationProvider> providers,
                                      MetricsService metrics) {
        super(notificationRepository, providers, metrics);
    }

    @Override
    protected void validateRecipient(NotificationRequest request) {
        if (request.recipient() == null || request.recipient().isBlank()) {
            throw new IllegalArgumentException("Recipient cannot be empty");
        }
        if (request.channel() == NotificationChannel.EMAIL
            && !EMAIL_PATTERN.matcher(request.recipient()).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + request.recipient());
        }
    }
}
