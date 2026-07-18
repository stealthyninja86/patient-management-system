package com.pms.notificationservice.service.adapter;

import com.pms.notificationservice.dto.event.NotificationMessage;
import com.pms.notificationservice.model.NotificationChannel;

public interface NotificationProvider {
    NotificationChannel supportedChannel();
    void send(NotificationMessage request);
}
