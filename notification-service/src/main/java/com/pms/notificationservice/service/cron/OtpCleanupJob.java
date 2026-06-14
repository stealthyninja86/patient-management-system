package com.pms.notificationservice.service.cron;

import com.pms.notificationservice.service.OtpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OtpCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(OtpCleanupJob.class);

    private final OtpService otpService;

    public OtpCleanupJob(OtpService otpService) {
        this.otpService = otpService;
    }

    /**
     * Runs every 5 minutes to expire OTPs whose TTL has passed.
     * Redis auto-evicts the cache entries, but the DB records need cleanup.
     */
    @Scheduled(fixedRate = 60000)
    public void cleanOtps(){
        log.debug("Cleaning up OTPs");
        otpService.expireStaleOtps();
    }
}
