package com.pms.notificationservice.service.adapter;

import com.pms.notificationservice.dto.request.NotificationRequest;
import com.pms.notificationservice.model.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "notification.email.provider", havingValue = "mock", matchIfMissing = true)
public class EmailChannel implements  NotificationProvider {
    private static final Logger log = LoggerFactory.getLogger(EmailChannel.class);

    @Override
    public NotificationChannel supportedChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void send(NotificationRequest request) {
        log.info(" [MOCK EMAIL] sending notification to: {} , message {}", request.recipient(), request.message());
    }
}
