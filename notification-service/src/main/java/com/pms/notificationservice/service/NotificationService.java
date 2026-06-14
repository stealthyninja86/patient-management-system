package com.pms.notificationservice.service;

import com.pms.notificationservice.service.adapter.NotificationProvider;
import com.pms.notificationservice.dto.request.NotificationRequest;
import com.pms.notificationservice.model.Notification;
import com.pms.notificationservice.model.NotificationChannel;
import com.pms.notificationservice.model.NotificationStatus;
import com.pms.notificationservice.repository.NotificationRepository;
import com.pms.notificationservice.service.factory.NotificationFactory;
import io.micrometer.core.instrument.Counter;
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
    private final Counter notificationSentCounter;
    private final Counter notificationFailedCounter;
    private final Counter dedupHitCounter;
    private final Counter dedupMissCounter;
    private final NotificationFactory notificationFactory;

    public NotificationService(NotificationRepository notificationRepository,
                               StringRedisTemplate redisTemplate,
                               List<NotificationProvider> providers,
                               Counter notificationSentCounter,
                               Counter notificationFailedCounter,
                               Counter dedupHitCounter,
                               Counter dedupMissCounter,
                               NotificationFactory notificationFactory) {
        this.notificationRepository = notificationRepository;
        this.redisTemplate = redisTemplate;
        this.providers = providers;
        this.notificationSentCounter = notificationSentCounter;
        this.notificationFailedCounter = notificationFailedCounter;
        this.dedupHitCounter = dedupHitCounter;
        this.dedupMissCounter = dedupMissCounter;
        this.notificationFactory = notificationFactory;
    }

    @PostConstruct
    private void initProviderMap(){
        for(NotificationProvider provider: providers){
            providerMap.put(provider.supportedChannel(), provider);
        }
    }

    /**
     * Send a notification with Redis dedup check.
     * Uses SETNX to prevent double-send on consumer retries.
     *
     * @return true if sent, false if dedup skipped
     */
    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
   @Transactional
   public boolean sendNotification(NotificationRequest notificationRequest){
        if (notificationRequest.recipient() == null || notificationRequest.recipient().isBlank()) {
            throw new IllegalArgumentException("Recipient cannot be empty");
        }
        String dedupKey = DEDUP_KEY_PREFIX + notificationRequest.eventId() + ":" + notificationRequest.channel().name();

        //check if already sent
        Boolean wasSet = redisTemplate.opsForValue()
                .setIfAbsent(dedupKey, "1", dedupTtlHours, TimeUnit.HOURS);

        if(Boolean.FALSE.equals(wasSet)){
            log.info("Duplicate notification suppressed: eventId={}, channel={}", notificationRequest.eventId(), notificationRequest.channel());
            dedupHitCounter.increment();
            return false;
        }

        dedupMissCounter.increment();

        //create notification record
        Notification notification = notificationFactory.createNotification(notificationRequest);

        try{
            // look up provider by channel and send
            NotificationProvider provider = providerMap.get(notificationRequest.channel());
            if (provider == null){
                throw new IllegalArgumentException(
                        "No provider found in channel: " + notificationRequest.channel()
                );
            }
            provider.send(notificationRequest);
            notificationSentCounter.increment();
            //mark as sent
            log.info("Notification sent for patientId: {}, with message: {}", notificationRequest.patientId(), notificationRequest.message());
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
        catch (Exception e){
            //mark as failed
            log.error("failed to send notification for patient id:{} with message:{}", notification.getPatientId(), e.getMessage());
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
            notificationRepository.save(notification);

            throw e;
        }
        return true;
    }

    /**
     * Called when all retry attempts are exhausted.
     * The notification is already marked FAILED (from the catch block),
     * but we keep the dedup key so it's not retried via Kafka reprocessing.
     */
    @Recover
    public boolean recover(Exception e, NotificationRequest notificationRequest){
        notificationFailedCounter.increment();
        log.error("All notification retries exhausted: eventId={}, channel={}, error={}", notificationRequest.eventId(), notificationRequest.channel(), e.getMessage());
        return false;
    }

    public List<Notification> getPatientNotificationHistory(String patientId){
        return notificationRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

}
