package com.pms.clinicalservice.service.pdf;

import com.pms.clinicalservice.kafka.PrescriptionPdfGeneratedEvent;
import com.pms.clinicalservice.kafka.PrescriptionPdfTaskEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PdfEventProducer {

    private static final Logger log = LoggerFactory.getLogger(PdfEventProducer.class);
    private static final String COMPLETION_TOPIC = "prescription-pdf-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PdfEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPdfGeneratedEvent(String prescriptionId, String patientId, String patientEmail,
                                       String doctorId, String hospitalId, String status) {
        PrescriptionPdfGeneratedEvent event = new PrescriptionPdfGeneratedEvent(
            prescriptionId, patientId, patientEmail, doctorId, hospitalId, status);
        kafkaTemplate.send(COMPLETION_TOPIC, prescriptionId, event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to send completion event for: {}", prescriptionId, ex);
                } else {
                    log.debug("Completion event sent for: {}", prescriptionId);
                }
            });
    }

    public void sendPdfTaskEvent(String prescriptionId) {
        PrescriptionPdfTaskEvent event = new PrescriptionPdfTaskEvent(prescriptionId);
        kafkaTemplate.send("prescription-pdf-tasks", prescriptionId, event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to send task event for: {}", prescriptionId, ex);
                }
            });
    }
}
