package com.pms.notificationservice.service.strategy;

import com.pms.notificationservice.model.NotificationChannel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PrescriptionChannelStrategy implements NotificationChannelStrategy {
    @Override
    public List<NotificationChannel> resolveChannels() {
        return List.of(NotificationChannel.EMAIL);
    }
}
