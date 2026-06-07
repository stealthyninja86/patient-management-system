package com.pms.notificationservice.adapter;

import com.pms.notificationservice.dto.NotificationRequest;
import com.pms.notificationservice.model.NotificationChannel;

public interface NotificationProvider {
    NotificationChannel supportedChannel();
    void send(NotificationRequest request);
}
