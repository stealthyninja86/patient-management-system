package com.pms.notificationservice.service.template;

import com.pms.notificationservice.dto.request.NotificationRequest;
import com.pms.notificationservice.model.NotificationChannel;
import com.pms.notificationservice.model.NotificationType;

import java.util.List;

public abstract class NotificationMessageTemplate<T> {

    public final List<NotificationRequest> createRequests(T event, List<NotificationChannel> channels) {
        return channels.stream()
                .map(channel -> createRequest(event, channel))
                .toList();
    }

    public final NotificationRequest createRequest(T event, NotificationChannel channel) {
        return new NotificationRequest(
                buildDedupKey(event, channel),
                getPatientId(event),
                getNotificationType(),
                channel,
                resolveRecipient(event, channel),
                buildMessage(event, channel)
        );
    }

    protected abstract NotificationType getNotificationType();

    protected abstract String getPatientId(T event);

    protected abstract String buildMessage(T event, NotificationChannel channel);

    protected abstract String resolveRecipient(T event, NotificationChannel channel);

    protected abstract String buildDedupKey(T event, NotificationChannel channel);
}
