package com.pms.notificationservice.strategy;

import com.pms.notificationservice.model.NotificationChannel;

import java.util.List;

@FunctionalInterface
public interface NotificationChannelStrategy {
    List<NotificationChannel> resolveChannels();
}
