package com.pms.notificationservice.kafka;

import com.pms.notificationservice.dto.NotificationRequest;
import com.pms.notificationservice.dto.PrescriptionPdfGeneratedEventDTO;
import com.pms.notificationservice.model.NotificationChannel;
import com.pms.notificationservice.model.NotificationType;
import com.pms.notificationservice.service.NotificationService;
import com.pms.notificationservice.strategy.ChannelRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class PrescriptionPdfEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PrescriptionPdfEventConsumer.class);

    private final NotificationService notificationService;
    private final ChannelRouter channelRouter;

    public PrescriptionPdfEventConsumer(NotificationService notificationService, ChannelRouter channelRouter) {
        this.notificationService = notificationService;
        this.channelRouter = channelRouter;
    }

    @KafkaListener(topics = "prescription-pdf-events")
    public void consume(PrescriptionPdfGeneratedEventDTO event){
        log.info("Received Prescription PDF event: prescriptionId = {} , status = {}", event.prescriptionId(), event.status());

        try{
            if (!"SUCCESS".equals(event.status())) {
                return;
            }

            if(event.patientEmail() == null || event.patientEmail().isBlank()){
                log.warn("Skopping prescription notification - no patient email : prescriptionId = {} ", event.prescriptionId());
                return;
            }

            String eventId = "rx-" + event.prescriptionId();
            NotificationType type = NotificationType.PRESCRIPTION_READY;
            String message = String.format(
                    "Your prescription (Id: %s) is ready. Please login to view and download",
                    event.prescriptionId()
            );

            for(NotificationChannel channel: channelRouter.resolve(type)){
                notificationService.sendNotification(new NotificationRequest(
                        eventId + ":" + channel.name().toLowerCase(),
                        event.patientId(), type, channel,
                        event.patientEmail(), message
                ));
            }

            log.info("prescription ready notification sent: prescriptionId = {} ", event.prescriptionId());
        } catch (Exception e) {
            log.error("Failed to process prescription event: prescriptionId = {} , error = {}", event.prescriptionId(), e.getMessage());
        }
    }
}
