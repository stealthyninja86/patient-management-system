package com.pms.clinicalservice.service.pdf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.clinicalservice.kafka.PrescriptionPdfGeneratedEvent;
import com.pms.clinicalservice.kafka.PrescriptionPdfTaskEvent;
import com.pms.clinicalservice.model.OutboxEvent;
import com.pms.clinicalservice.repository.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PdfEventProducer {

    private static final Logger log = LoggerFactory.getLogger(PdfEventProducer.class);
    private static final String COMPLETION_TOPIC = "prescription-pdf-events";
    private static final String TASK_TOPIC = "prescription-pdf-tasks";

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public PdfEventProducer(OutboxRepository outboxRepository,
                            ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    public void sendPdfGeneratedEvent(String prescriptionId, String patientId, String patientName,
                                       String patientEmail, String doctorId, String doctorName,
                                       String hospitalId, String hospitalName, String status) {
        try {
            PrescriptionPdfGeneratedEvent event = new PrescriptionPdfGeneratedEvent(
                prescriptionId, patientId, patientName, patientEmail,
                doctorId, doctorName, hospitalId, hospitalName, status);
            String payload = objectMapper.writeValueAsString(event);
            OutboxEvent outboxEvent = new OutboxEvent(
                UUID.randomUUID(),
                "PRESCRIPTION",
                prescriptionId,
                "PRESCRIPTION_PDF_GENERATED",
                COMPLETION_TOPIC,
                payload,
                prescriptionId,
                false,
                LocalDateTime.now(),
                null
            );
            outboxRepository.save(outboxEvent);
            log.debug("Outbox event saved: PRESCRIPTION_PDF_GENERATED for prescription: {}", prescriptionId);
        } catch (Exception e) {
            log.error("Failed to write outbox event for prescription: {}", prescriptionId, e);
        }
    }

    public void sendPdfTaskEvent(String prescriptionId) {
        try {
            PrescriptionPdfTaskEvent event = new PrescriptionPdfTaskEvent(prescriptionId);
            String payload = objectMapper.writeValueAsString(event);
            OutboxEvent outboxEvent = new OutboxEvent(
                UUID.randomUUID(),
                "PRESCRIPTION",
                prescriptionId,
                "PRESCRIPTION_PDF_TASK",
                TASK_TOPIC,
                payload,
                prescriptionId,
                false,
                LocalDateTime.now(),
                null
            );
            outboxRepository.save(outboxEvent);
            log.debug("Outbox event saved: PRESCRIPTION_PDF_TASK for prescription: {}", prescriptionId);
        } catch (Exception e) {
            log.error("Failed to write outbox event for prescription task: {}", prescriptionId, e);
        }
    }
}
