package com.pms.scheduleservice.service.cron;

import com.pms.scheduleservice.repository.AppointmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class CancelledAppointmentCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(CancelledAppointmentCleanupJob.class);

    private static final int CUTOFF_HOURS = 24;
    private final AppointmentRepository appointmentRepository;

    public CancelledAppointmentCleanupJob(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void purgeCancelledAppointments() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(CUTOFF_HOURS);
        int deleted = appointmentRepository.purgeCancelledAppointments(cutoff);
        if (deleted > 0) {
            log.info("purged {} cancelled appointments whose time slot ended before {}", deleted, cutoff);
        }
    }
}
