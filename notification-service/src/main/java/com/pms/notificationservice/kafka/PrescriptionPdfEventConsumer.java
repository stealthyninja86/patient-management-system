package com.pms.notificationservice.kafka;

import com.pms.notificationservice.dto.event.PrescriptionPdfGeneratedEventDTO;
import com.pms.notificationservice.model.NotificationType;
import com.pms.notificationservice.service.NotificationService;
import com.pms.notificationservice.service.resolver.NameResolver;
import com.pms.notificationservice.service.strategy.ChannelRouter;
import com.pms.notificationservice.service.template.PrescriptionReadyTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

@Service
public class PrescriptionPdfEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PrescriptionPdfEventConsumer.class);

    private final NotificationService notificationService;
    private final ChannelRouter channelRouter;
    private final PrescriptionReadyTemplate prescriptionTemplate;
    private final NameResolver nameResolver;

    public PrescriptionPdfEventConsumer(NotificationService notificationService,
                                         ChannelRouter channelRouter,
                                         PrescriptionReadyTemplate prescriptionTemplate,
                                         NameResolver nameResolver) {
        this.notificationService = notificationService;
        this.channelRouter = channelRouter;
        this.prescriptionTemplate = prescriptionTemplate;
        this.nameResolver = nameResolver;
    }

    @RetryableTopic(
        attempts = "4",
        backoff = @Backoff(delay = 2000, multiplier = 2.0, maxDelay = 30000),
        dltTopicSuffix = "-dlt"
    )
    @KafkaListener(topics = "prescription-pdf-events", containerFactory = "prescriptionKafkaListenerContainerFactory")
    public void consume(PrescriptionPdfGeneratedEventDTO event){
        if (event == null) {
            log.warn("Received null prescription event, skipping");
            return;
        }

        log.info("Received Prescription PDF event: prescriptionId = {} , status = {}", event.prescriptionId(), event.status());

        try{
            if (!"SUCCESS".equals(event.status())) {
                return;
            }

            if(event.patientEmail() == null || event.patientEmail().isBlank()){
                log.warn("Skipping prescription notification - no patient email : prescriptionId = {} ", event.prescriptionId());
                return;
            }

            PrescriptionPdfGeneratedEventDTO resolved = resolveNames(event);

            var requests = prescriptionTemplate.createRequests(resolved,
                    channelRouter.resolve(NotificationType.PRESCRIPTION_READY));

            for(var request : requests){
                notificationService.sendNotification(request);
            }

            log.info("prescription ready notification sent: prescriptionId = {} ", event.prescriptionId());
        } catch (Exception e) {
            log.error("Failed to process prescription event: prescriptionId = {} , error = {}", event.prescriptionId(), e.getMessage());
        }
    }

    @DltHandler
    public void handleDlt(PrescriptionPdfGeneratedEventDTO event) {
        log.error("Prescription PDF event moved to DLT after retries exhausted: prescriptionId={}", event != null ? event.prescriptionId() : "null");
    }

    private PrescriptionPdfGeneratedEventDTO resolveNames(PrescriptionPdfGeneratedEventDTO event) {
        String patientName = event.patientName() != null ? event.patientName() : nameResolver.resolvePatientName(event.patientId());
        String doctorName = event.doctorName() != null ? event.doctorName() : nameResolver.resolveDoctorName(event.doctorId());
        String hospitalName = event.hospitalName() != null ? event.hospitalName() : nameResolver.resolveHospitalName(event.hospitalId());

        if (patientName != null || doctorName != null || hospitalName != null) {
            log.debug("Resolved names for prescriptionId={}: patientName={}, doctorName={}, hospitalName={}",
                    event.prescriptionId(), patientName, doctorName, hospitalName);
        }

        return new PrescriptionPdfGeneratedEventDTO(
                event.prescriptionId(),
                event.patientId(),
                patientName != null ? patientName : event.patientName(),
                doctorName != null ? doctorName : event.doctorName(),
                hospitalName != null ? hospitalName : event.hospitalName(),
                event.patientEmail(),
                event.status(),
                event.doctorId(),
                event.hospitalId()
        );
    }
}
