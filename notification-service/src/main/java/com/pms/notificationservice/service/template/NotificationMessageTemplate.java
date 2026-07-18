package com.pms.notificationservice.service.template;

import com.pms.notificationservice.dto.event.NotificationMessage;
import com.pms.notificationservice.model.NotificationChannel;
import com.pms.notificationservice.model.NotificationType;

import java.util.List;

public abstract class NotificationMessageTemplate<T> {

    public final List<NotificationMessage> createRequests(T event, List<NotificationChannel> channels) {
        return channels.stream()
                .map(channel -> createRequest(event, channel))
                .toList();
    }

    public abstract NotificationMessage createRequest(T event, NotificationChannel channel);

    public abstract NotificationType getNotificationType();

    protected abstract String getPatientId(T event);

    protected abstract String buildMessage(T event, NotificationChannel channel);

    protected abstract String resolveRecipient(T event, NotificationChannel channel);

    protected abstract String buildDedupKey(T event, NotificationChannel channel);
}
