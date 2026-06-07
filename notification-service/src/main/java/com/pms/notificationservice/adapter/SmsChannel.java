package com.pms.notificationservice.adapter;

import com.pms.notificationservice.dto.NotificationRequest;
import com.pms.notificationservice.model.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "notification.sms.provider", havingValue = "mock", matchIfMissing = true)
public class SmsChannel implements NotificationProvider {

    private static final Logger log = LoggerFactory.getLogger(SmsChannel.class);

    @Override
    public NotificationChannel supportedChannel() {
        return NotificationChannel.SMS;
    }

    @Override
    public void send(NotificationRequest request) {
        log.info("📱[SMS MOCK] sending notification to: {} , message {}", request.recipient(), request.message());
    }
}
