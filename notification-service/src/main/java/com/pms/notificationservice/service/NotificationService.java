package com.pms.notificationservice.service;

import com.pms.notificationservice.dto.event.AppointmentConfirmationNotification;
import com.pms.notificationservice.dto.event.ConsentOtpNotification;
import com.pms.notificationservice.dto.event.NotificationMessage;
import com.pms.notificationservice.dto.event.PrescriptionReadyNotification;
import com.pms.notificationservice.service.adapter.NotificationProvider;
import com.pms.notificationservice.model.Notification;
import com.pms.notificationservice.model.NotificationChannel;
import com.pms.notificationservice.model.NotificationStatus;
import com.pms.notificationservice.repository.NotificationRepository;
import com.pms.notificationservice.service.mapper.NotificationMapper;
import com.pms.notificationservice.service.metrics.MetricsService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private static final String DEDUP_KEY_PREFIX = "dedup:";

    @Value("${notification.dedup.ttl-hours:1}")
    private int dedupTtlHours;

    private final NotificationRepository notificationRepository;
    private final StringRedisTemplate redisTemplate;
    private final List<NotificationProvider> providers;
    private final Map<NotificationChannel, NotificationProvider> providerMap = new EnumMap<>(NotificationChannel.class);
    private final MetricsService metrics;
    private final NotificationMapper notificationMapper;

    public NotificationService(NotificationRepository notificationRepository,
                                StringRedisTemplate redisTemplate,
                                List<NotificationProvider> providers,
                                MetricsService metrics,
                                NotificationMapper notificationMapper) {
        this.notificationRepository = notificationRepository;
        this.redisTemplate = redisTemplate;
        this.providers = providers;
        this.metrics = metrics;
        this.notificationMapper = notificationMapper;
    }

    @PostConstruct
    private void initProviderMap(){
        for(NotificationProvider provider: providers){
            providerMap.put(provider.supportedChannel(), provider);
        }
    }

    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
   @Transactional
   public boolean sendNotification(NotificationMessage message){
        if (message.recipient() == null || message.recipient().isBlank()) {
            throw new IllegalArgumentException("Recipient cannot be empty");
        }
        String dedupKey = DEDUP_KEY_PREFIX + message.eventId() + ":" + message.channel().name();

        Boolean wasSet = redisTemplate.opsForValue()
                .setIfAbsent(dedupKey, "1", dedupTtlHours, TimeUnit.HOURS);

        if(Boolean.FALSE.equals(wasSet)){
            log.info("Duplicate notification suppressed: eventId={}, channel={}", message.eventId(), message.channel());
            metrics.recordDedupHit();
            return false;
        }

        metrics.recordDedupMiss();

        Notification notification = notificationMapper.createNotification(message);

        try{
            NotificationProvider provider = providerMap.get(message.channel());
            if (provider == null){
                throw new IllegalArgumentException(
                        "No provider found in channel: " + message.channel()
                );
            }
            provider.send(message);
            metrics.recordNotificationSent(message.channel().name());
            log.info("Notification sent for patientId: {}, type: {}", message.patientId(), message.type());
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
        catch (Exception e){
            log.error("failed to send notification for patient id:{} with message:{}", notification.getPatientId(), e.getMessage());
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
            notificationRepository.save(notification);

            throw e;
        }
        return true;
    }

    @Recover
    public boolean recover(Exception e, NotificationMessage message){
        metrics.recordNotificationFailed(message.channel().name());
        log.error("All notification retries exhausted: eventId={}, channel={}, error={}", message.eventId(), message.channel(), e.getMessage());
        return false;
    }

    public List<Notification> getPatientNotificationHistory(String patientId){
        return notificationRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

}
