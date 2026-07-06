package com.pms.scheduleservice.service.cron;

import com.pms.scheduleservice.model.AppointmentStatus;
import com.pms.scheduleservice.repository.AppointmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class AppointmentCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(AppointmentCleanupJob.class);

    private static final int PENDING_EXPIRY_MINUTES = 5;
    private final AppointmentRepository appointmentRepository;

    public AppointmentCleanupJob(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void expirePendingOtps(){
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(PENDING_EXPIRY_MINUTES);
        int cancelled = appointmentRepository.expireByStatusAndCreatedBefore(
                AppointmentStatus.PENDING_OTP, cutoff
        );

        if(cancelled > 0){
            log.info("cleanup: cancelled {} stale PENDING_OTPs appointments", cancelled);
        }
    }
}
