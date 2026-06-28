package com.pms.notificationservice.service;

import com.pms.notificationservice.service.adapter.OtpNotificationTemplate;
import com.pms.notificationservice.dto.request.NotificationRequest;
import com.pms.notificationservice.model.NotificationChannel;
import com.pms.notificationservice.model.NotificationType;
import com.pms.notificationservice.model.Otp;
import com.pms.notificationservice.model.OtpStatus;
import com.pms.notificationservice.repository.OtpRepository;
import com.pms.notificationservice.service.factory.OtpCreator;
import com.pms.notificationservice.service.metrics.MetricsService;
import com.pms.notificationservice.service.strategy.ChannelRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    private static final int OTP_TTL_SECONDS = 180;
    private static final int MAX_ATTEMPTS = 3;
    private static final String REDIS_KEY_PREFIX = "otp:";

    private final OtpRepository otpRepository;
    private final StringRedisTemplate redisTemplate;
    private final OtpNotificationTemplate otpNotificationTemplate;
    private final ChannelRouter channelRouter;
    private final SecureRandom secureRandom;
    private final MetricsService metrics;
    private final OtpCreator otpFactory;

    public OtpService(OtpRepository otpRepository,
                      StringRedisTemplate redisTemplate,
                      OtpNotificationTemplate otpNotificationTemplate,
                      ChannelRouter channelRouter,
                      SecureRandom secureRandom,
                      MetricsService metrics,
                      OtpCreator otpFactory) {
        this.otpRepository = otpRepository;
        this.redisTemplate = redisTemplate;
        this.otpNotificationTemplate = otpNotificationTemplate;
        this.channelRouter = channelRouter;
        this.secureRandom = secureRandom;
        this.metrics = metrics;
        this.otpFactory = otpFactory;
    }

    /**
     * Generate a 6-digit OTP, persist metadata to DB, cache code in Redis, and send via SMS.
     *
     * The actual code lives ONLY in Redis with TTL. PostgreSQL stores metadata for
     * audit trail, attempt counting, and state machine durability.
     * Returns the otpId (not the code) so the verify endpoint can look it up.
     * The code is sent to the patient via SMS — never returned in the API response.
     *
     * @param patientId  the patient requesting consent
     * @param doctorId   the doctor requesting access
     * @param hospitalId the facility where consent is requested
     * @param consentRequestId  optional external tracking ID
     * @param phoneNumber the patient's phone number for SMS delivery (not stored raw)
     * @return the generated OTP entity ID
     */
    @Transactional
    public UUID generateOtp(String patientId, String doctorId,
                            String hospitalId, String consentRequestId,
                            String phoneNumber){
        Otp otp = otpFactory.createOtp(
                patientId, doctorId, hospitalId, consentRequestId,
                phoneNumber, Instant.now().plusSeconds(OTP_TTL_SECONDS));
        String code =  String.format("%06d", secureRandom.nextInt(1_000_000));
        otpRepository.save(otp);
            metrics.recordOtpGenerated();

        String redisKey = REDIS_KEY_PREFIX + otp.getId().toString();
        redisTemplate.opsForValue().set(redisKey, code, OTP_TTL_SECONDS, TimeUnit.SECONDS);

        String message = String.format(
                "your consent verfication code is: %s . Valid for 2 minutes" +
                        "if you did not request this, please ignore.", code
        );

        for(NotificationChannel channel: channelRouter.resolve(NotificationType.CONSENT_OTP)){
            NotificationRequest request = new NotificationRequest(
                    otp.getId().toString(),
                    patientId,
                    NotificationType.CONSENT_OTP,
                    channel,
                    phoneNumber,
                    message
                    );
            otpNotificationTemplate.send(request);
        }

        log.info("OTP generated: otpId={}, patientId={}, doctorId={}, code={}",  otp.getId().toString(), patientId, doctorId, code);
        return otp.getId();
    }

    /**
     * Verify an OTP code. Checks Redis first (fast path only — code is NEVER in DB).
     *
     * The actual OTP code lives exclusively in Redis. If Redis misses (TTL expired or
     * server unavailable), we cannot compare codes and return EXPIRED. DB is checked
     * only for terminal status (already VERIFIED/LOCKED) and attempt counting.
     *
     * Returns the OtpStatus after verification:
     *   VERIFIED — code correct and within TTL
     *   EXPIRED  — TTL passed or Redis unavailable (code unreachable)
     *   LOCKED   — 3 failed attempts
     */
    @Transactional
    public OtpStatus verifyOtp(UUID otpId, String code){
        //check Redis cache
        String rediskey = REDIS_KEY_PREFIX + otpId.toString();
        String cachedCode = redisTemplate.opsForValue().get(rediskey);

        if(cachedCode == null){
            log.warn("OTP code not found in Redis, likely expired: otpId={}", otpId);
            return handleRedisMiss(otpId);
        }

        if(!cachedCode.equals(code)){
            log.warn("OTP mismatch: otpId={}, code={}", otpId, code);
            return handleMismatch(otpId);
        }

            metrics.recordOtpVerified();
        return updateStatus(otpId, OtpStatus.VERIFIED);
    }

    private OtpStatus handleRedisMiss(UUID otpId){
        Otp otp = otpRepository.findById(otpId).orElseThrow(
                () -> new IllegalArgumentException("OTP not found: " + otpId));

        if(otp.getStatus() == OtpStatus.VERIFIED){
            return OtpStatus.VERIFIED;
        }

        if(otp.getStatus() == OtpStatus.LOCKED){
            return OtpStatus.LOCKED;
        }

        //cod is gone from Redis and OTP isnt already in a termainal state - expired
        return updateStatus(otpId, OtpStatus.EXPIRED);
    }

    private OtpStatus handleMismatch(UUID otpId){
        Otp otp = otpRepository.findById(otpId).orElseThrow(
                () -> new IllegalArgumentException("OTP not found: " + otpId)
        );

        int newAttempts = otp.getAttempts() + 1;
        otp.setAttempts(newAttempts);
        otpRepository.save(otp);

        if(newAttempts >= MAX_ATTEMPTS){
            return updateStatus(otpId, OtpStatus.LOCKED);
        }

        log.warn("OTP verification failed: otpId:{} , attempts={} , maxAttempts:{}", otpId, newAttempts,  MAX_ATTEMPTS);
        return OtpStatus.GENERATED;
    }

    private OtpStatus updateStatus(UUID otpId, OtpStatus newStatus){
        Otp otp = otpRepository.findById(otpId).orElseThrow(
                () -> new IllegalArgumentException("OTP not found: " + otpId)
        );
        otp.setStatus(newStatus);
        if (newStatus == OtpStatus.VERIFIED) {
            otp.setVerifiedAt(Instant.now());
        }
        else if (newStatus == OtpStatus.EXPIRED || newStatus == OtpStatus.LOCKED) {
            metrics.recordOtpFailed();
        }

        //Remove from Redis cache if terminal
        String redisKey = REDIS_KEY_PREFIX + otpId.toString();
        redisTemplate.delete(redisKey);

        log.info("OTP status updated: otpId={}, newStatus={}", otpId, newStatus);
        return newStatus;
    }

    /**
     * Expire OTPs whose TTL has passed but DB records remain in GENERATED state.
     *
     * Redis auto-evicts the cache entry after TTL, but the PostgreSQL record
     * stays in GENERATED state indefinitely without this cleanup. Called by
     * {@code OtpCleanupJob} on a scheduled interval.
     */
    @Transactional
    public void expireStaleOtps(){
        Instant now = Instant.now();
        List<Otp> staleOtps = otpRepository.findAll().stream()
                .filter(
                        o -> o.getStatus() == OtpStatus.GENERATED && o.getExpiresAt().isBefore(now))
                .toList();

        for(Otp otp: staleOtps){
            otp.setStatus(OtpStatus.EXPIRED);
            otpRepository.save(otp);
            log.info("OTP expired via cleanup: otpId={}", otp.getId());
        }

        if(!staleOtps.isEmpty()){
            log.info("Expired {} stale otps left", staleOtps.size());
        }
    }

}
