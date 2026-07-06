package com.pms.notificationservice.service;

import com.pms.notificationservice.dto.event.OtpNotificationContext;
import com.pms.notificationservice.dto.response.OtpVerifyResult;
import com.pms.notificationservice.exception.OtpCoolDownException;
import com.pms.notificationservice.dto.request.NotificationRequest;
import com.pms.notificationservice.model.NotificationChannel;
import com.pms.notificationservice.model.NotificationType;
import com.pms.notificationservice.model.Otp;
import com.pms.notificationservice.model.OtpStatus;
import com.pms.notificationservice.repository.OtpRepository;
import com.pms.notificationservice.service.factory.OtpCreator;
import com.pms.notificationservice.service.metrics.MetricsService;
import com.pms.notificationservice.service.strategy.ChannelRouter;
import com.pms.notificationservice.service.template.NotificationMessageTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    private static final int OTP_TTL_SECONDS = 180;
    private static final int MAX_ATTEMPTS = 3;
    private static final String REDIS_KEY_PREFIX = "otp:";
    private static final String OTP_HASH_SALT = "pms-otp-salt2026";
    private final String LOCKED = OtpStatus.LOCKED.name();
    private final String EXPIRED = OtpStatus.EXPIRED.name();
    private final String VERIFIED = OtpStatus.VERIFIED.name();
    private final String GENERATED = OtpStatus.GENERATED.name();
    private final String MISMATCH = OtpStatus.MISMATCH.name();
    private final String SMS = NotificationChannel.SMS.name();
    private final String EMAIL = NotificationChannel.EMAIL.name();

    private final OtpRepository otpRepository;
    private final StringRedisTemplate redisTemplate;
    private final ChannelRouter channelRouter;
    private final SecureRandom secureRandom;
    private final MetricsService metrics;
    private final OtpCreator otpFactory;
    private final NotificationService notificationService;
    private final Map<NotificationType, NotificationMessageTemplate<OtpNotificationContext>> otpTemplates;

    public OtpService(OtpRepository otpRepository,
                      StringRedisTemplate redisTemplate,
                      ChannelRouter channelRouter,
                      SecureRandom secureRandom,
                      MetricsService metrics,
                      OtpCreator otpFactory,
                      NotificationService notificationService,
                      List<NotificationMessageTemplate<OtpNotificationContext>> otpTemplates) {
        this.otpRepository = otpRepository;
        this.redisTemplate = redisTemplate;
        this.channelRouter = channelRouter;
        this.secureRandom = secureRandom;
        this.metrics = metrics;
        this.otpFactory = otpFactory;
        this.notificationService = notificationService;
        this.otpTemplates = new HashMap<>();
        for (NotificationMessageTemplate<OtpNotificationContext> t : otpTemplates) {
            this.otpTemplates.put(t.getNotificationType(), t);
        }
    }

    /**
     * Generate a 6-digit OTP, persist metadata to DB, cache code in Redis, and send via SMS.
     *
     * The actual code lives ONLY in Redis with TTL. PostgreSQL stores metadata for
     * audit trail, attempt counting, and state machine durability.
     * Returns the otpId (not the code) so the verify endpoint can look it up.
     * The code is sent to the patient via SMS — never returned in the API response.
     *
     * @param domainKey   purpose-prefixed domain identifier (e.g. "consent:<uuid>")
     * @param phoneNumber the patient's phone number for SMS delivery (not stored raw)
     * @return the generated OTP entity ID
     */
    @Transactional
    public UUID generateOtp(String domainKey, String phoneNumber) {
        Otp otp = otpFactory.createOtp(
                domainKey, phoneNumber, Instant.now().plusSeconds(OTP_TTL_SECONDS));
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
                    domainKey,
                    NotificationType.CONSENT_OTP,
                    channel,
                    phoneNumber,
                    message
            );
            notificationService.sendNotification(request);
        }

        log.info("OTP generated: otpId={}, domainKey={}, code={}", otp.getId().toString(), domainKey, code);
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

    String hashCode(String code){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest((code + OTP_HASH_SALT).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available",e);
        }
    }

    public void generateOtpForDomain(String domainKey, String phoneNumber, String email, NotificationType type){
        String coolDownKey = "otp:cooldown:" + domainKey;
        if (redisTemplate.hasKey(coolDownKey)) {
            long ttl = redisTemplate.getExpire(coolDownKey);
            throw new OtpCoolDownException("please wait " + ttl + "'s before requesting a new code");
        }

        String globalAttemptsKey = "otp:attempts-global" + domainKey;
        String globalstr = redisTemplate.opsForValue().get(globalAttemptsKey);
        int globalAttempts = globalstr != null ? Integer.parseInt(globalstr) : 0;

        if(globalAttempts >= MAX_ATTEMPTS){
            throw  new OtpCoolDownException("Maximum verifcation attempts exceeded. Please try again later.");
        }

        String code = String.format("%06d", secureRandom.nextInt(1_000_000));
        String codeHash = hashCode(code);

        Map<String, String> otpData = new HashMap<>();
        otpData.put("hash", codeHash);
        otpData.put("status", GENERATED);
        otpData.put("attempts", "0");
        redisTemplate.opsForHash().putAll(domainKey, otpData);
        redisTemplate.expire(domainKey, OTP_TTL_SECONDS, TimeUnit.SECONDS);

        redisTemplate.opsForValue().set(coolDownKey, "1", 60, TimeUnit.SECONDS);
        log.info("OTP generated: key = {}", domainKey);

        // Look up the per-flow template and deliver via all resolved channels
        NotificationMessageTemplate<OtpNotificationContext> template = otpTemplates.get(type);
        OtpNotificationContext ctx = new OtpNotificationContext(
                domainKey, phoneNumber, email, code
        );

        for(NotificationChannel channel : channelRouter.resolve(type)){
            NotificationRequest request = template.createRequest(ctx, channel);
            notificationService.sendNotification(request);
        }
    }

    public OtpVerifyResult verifyOtpForDomain(String domainKey, String code){

        Map<Object, Object> otpData = redisTemplate.opsForHash().entries(domainKey);
        if(otpData.isEmpty()){
            log.info("OTP is already expired");
            return new OtpVerifyResult(false, EXPIRED, 0);
        }

        String status = (String) otpData.get("status");

        if(status.equals(VERIFIED) || status.equals(LOCKED)){
            log.info("OTP is already {}", status);
            return new OtpVerifyResult(false, status, 0);
        }

        int attempts = Integer.parseInt((String) otpData.get("attempts"));

        if (attempts >=  MAX_ATTEMPTS) {
            redisTemplate.opsForHash().put(domainKey, "status", LOCKED);
            log.info("attempts per opt exhausted: key = {}", domainKey);
            return new OtpVerifyResult(false, LOCKED, 0);
        }

        String globalAttemptsKey = "otp:attempts-global:" + domainKey;
        String globalstr = redisTemplate.opsForValue().get(globalAttemptsKey);
        int globalAttempts = globalstr != null ? Integer.parseInt(globalstr) : 0;

        if(globalAttempts >= MAX_ATTEMPTS){
            redisTemplate.opsForHash().put(domainKey, "status", LOCKED);
            log.info("global attemps exhausted: key = {}", domainKey);
            return new OtpVerifyResult(false, LOCKED, 0);
        }

        String storedHash = (String) otpData.get("hash");
        String inputHash = hashCode(code);

        if(MessageDigest.isEqual(storedHash.getBytes(StandardCharsets.UTF_8),
                inputHash.getBytes(StandardCharsets.UTF_8))){
            redisTemplate.opsForHash().put(domainKey, "status", VERIFIED);
            redisTemplate.delete("otp:cooldown:" + domainKey);
            log.info("OTP verified: key = {}", domainKey);
            return  new OtpVerifyResult(true, VERIFIED, 0);
        }

        attempts++;
        redisTemplate.opsForHash().put(domainKey, "attempts", attempts);
        redisTemplate.opsForValue().increment(globalAttemptsKey);

        Long globalTtl = redisTemplate.getExpire(globalAttemptsKey);
        if (globalTtl == null || globalTtl < 0){
            redisTemplate.expire(globalAttemptsKey, 24, TimeUnit.HOURS);
        }

        int remainingAttempts = MAX_ATTEMPTS - attempts;
        int remainingGlobalAttempts = MAX_ATTEMPTS - (globalAttempts + 1);

        if(remainingAttempts <= 0 ||  remainingGlobalAttempts <= 0){
            redisTemplate.opsForHash().put(domainKey, "status", LOCKED);
            log.warn("attempts per opt exhausted: key = {}", domainKey);
            return new OtpVerifyResult(false, LOCKED, 0);
        }

        log.warn("OTP mismatch: key = {}, attempts = {}/{}, global = {}/{}",
                domainKey, attempts, MAX_ATTEMPTS, globalAttempts + 1, MAX_ATTEMPTS);

        return new OtpVerifyResult(false, MISMATCH , remainingAttempts);
    }

}
