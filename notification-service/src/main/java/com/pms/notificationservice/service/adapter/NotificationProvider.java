package com.pms.notificationservice.service.adapter;

import com.pms.notificationservice.dto.request.NotificationRequest;
import com.pms.notificationservice.model.NotificationChannel;

public interface NotificationProvider {
    NotificationChannel supportedChannel();
    void send(NotificationRequest request);
}
