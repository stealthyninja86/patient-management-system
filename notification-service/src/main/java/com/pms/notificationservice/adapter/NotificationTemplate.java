package com.pms.notificationservice.adapter;

import com.pms.notificationservice.dto.NotificationRequest;
import com.pms.notificationservice.model.Notification;
import com.pms.notificationservice.model.NotificationChannel;
import com.pms.notificationservice.model.NotificationStatus;
import com.pms.notificationservice.repository.NotificationRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public abstract class NotificationTemplate {
    private static final Logger log = LoggerFactory.getLogger(NotificationTemplate.class);

    private final NotificationRepository notificationRepository;
    private final List<NotificationProvider> providers;
    private final Map<NotificationChannel, NotificationProvider> providerMap = new EnumMap<>(NotificationChannel.class);

    public NotificationTemplate(NotificationRepository notificationRepository, List<NotificationProvider> providers) {
        this.notificationRepository = notificationRepository;
        this.providers = providers;
    }

    @PostConstruct
    private void initProviderMap() {
        for (NotificationProvider provider : providers) {
            providerMap.put(provider.supportedChannel(), provider);
        }
    }

    public void send(NotificationRequest request) {
        Notification notification = Notification.builder()
                .patientId(request.patientId())
                .type(request.type())
                .channel(request.channel())
                .recipient(request.recipient())
                .message(request.message())
                .status(NotificationStatus.PENDING)
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);

        int maxAttempts = 3;
        long delay = 1000;
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                validateRecipient(request);

                NotificationProvider provider = providerMap.get(request.channel());
                if (provider == null) {
                    throw new IllegalArgumentException("No provider found for channel: " + request.channel());
                }

                log.info("Sending notification (attempt {}/{}): {}", attempt, maxAttempts, notification.getRecipient());
                provider.send(request);

                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
                notification.setRetryCount(attempt - 1);
                notificationRepository.save(notification);
                log.info("Notification sent successfully type:{} , channel: {}, recipient: {}",
                        request.type(), request.channel(), request.recipient());
                return;

            } catch (Exception e) {
                lastException = e;
                log.warn("Attempt {}/{} failed: type={}, channel={}, error={}",
                        attempt, maxAttempts, request.type(), request.channel(), e.getMessage());

                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                    delay *= 2;
                }
            }
        }

        notification.setStatus(NotificationStatus.FAILED);
        notification.setErrorMessage(lastException.getMessage());
        notification.setRetryCount(maxAttempts - 1);
        notificationRepository.save(notification);
        log.error("All retries exhausted: type={}, channel={}, recipient={}",
                request.type(), request.channel(), request.recipient());
    }

    protected abstract void validateRecipient(NotificationRequest request);
}
