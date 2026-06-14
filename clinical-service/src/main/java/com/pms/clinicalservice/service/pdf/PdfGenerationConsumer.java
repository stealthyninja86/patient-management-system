package com.pms.clinicalservice.service.pdf;

import com.pms.clinicalservice.kafka.PrescriptionPdfTaskEvent;
import com.pms.clinicalservice.model.DocumentStatus;
import com.pms.clinicalservice.model.Prescription;
import com.pms.clinicalservice.model.PrescriptionDocument;
import com.pms.clinicalservice.repository.PrescriptionDocumentRepository;
import com.pms.clinicalservice.repository.PrescriptionRepository;
import com.pms.clinicalservice.service.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;

@Service
public class PdfGenerationConsumer {

    private static final Logger log = LoggerFactory.getLogger(PdfGenerationConsumer.class);

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionDocumentRepository documentRepository;
    private final StorageService storageService;
    private final AgeCalculator ageCalculator;
    private final SpringTemplateEngine templateEngine;
    private final PdfEventProducer eventProducer;

    public PdfGenerationConsumer(PrescriptionRepository prescriptionRepository,
                                  PrescriptionDocumentRepository documentRepository,
                                  StorageService storageService,
                                  AgeCalculator ageCalculator,
                                  SpringTemplateEngine templateEngine,
                                  PdfEventProducer eventProducer) {
        this.prescriptionRepository = prescriptionRepository;
        this.documentRepository = documentRepository;
        this.storageService = storageService;
        this.ageCalculator = ageCalculator;
        this.templateEngine = templateEngine;
        this.eventProducer = eventProducer;
    }


    @RetryableTopic(
        attempts = "4",
        backoff = @Backoff(delay = 2000, multiplier = 2.0),
        dltTopicSuffix = "-dlt"
    )
    @KafkaListener(topics = "prescription-pdf-tasks")
    @Transactional
    public void consume(PrescriptionPdfTaskEvent event) {
        String prescriptionId = event.getPrescriptionId();
        log.info("Consuming PDF task for prescription: {}", prescriptionId);

        try {
            Prescription prescription = prescriptionRepository.findByPrescriptionId(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Prescription not found: " + prescriptionId));

            String age = ageCalculator.calculateAge(prescription.getPatientDateOfBirth());

            Context context = new Context();
            context.setVariable("prescriptionId", prescription.getPrescriptionId());
            context.setVariable("patientName", prescription.getPatientName());
            context.setVariable("patientId", prescription.getPatientId());
            context.setVariable("patientPhone", prescription.getPatientPhone());
            context.setVariable("patientEmail", prescription.getPatientEmail());
            context.setVariable("patientGender", prescription.getPatientGender());
            context.setVariable("patientDob", prescription.getPatientDateOfBirth());
            context.setVariable("age", age);
            context.setVariable("doctorName", prescription.getDoctorName());
            context.setVariable("doctorEmail", prescription.getDoctorEmail());
            context.setVariable("doctorPhone", prescription.getDoctorPhone());
            context.setVariable("departmentName", prescription.getDepartmentName());
            context.setVariable("hospitalName", prescription.getHospitalName());
            context.setVariable("hospitalPhone", prescription.getHospitalPhone());
            context.setVariable("hospitalEmail", prescription.getHospitalEmail());
            context.setVariable("hospitalWebsite", prescription.getHospitalWebsite());
            context.setVariable("hospitalAddress", prescription.getHospitalAddress());
            context.setVariable("diagnosis", prescription.getDiagnosis());
            context.setVariable("allergies", prescription.getAllergies());
            context.setVariable("painScore", prescription.getPainScore());
            context.setVariable("doctorNote", prescription.getDoctorNote());
            context.setVariable("drugs", prescription.getDrugs());
            context.setVariable("followUpWeeks", prescription.getFollowUpWeeks());
            context.setVariable("consultationDate", prescription.getConsultationDate());
            context.setVariable("generatedAt", LocalDateTime.now());

            String html = templateEngine.process("prescription-template", context);

            byte[] pdfBytes;
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ITextRenderer renderer = new ITextRenderer();
                renderer.setDocumentFromString(html);
                renderer.layout();
                renderer.createPDF(baos);
                pdfBytes = baos.toByteArray();
            }

            //metadata
            String fileName = prescriptionId + ".pdf";
            PrescriptionDocument doc = documentRepository.findByPrescriptionId(prescriptionId)
                .orElse(PrescriptionDocument.builder()
                    .prescriptionId(prescriptionId)
                    .fileName(fileName)
                    .build());
            doc.setGeneratedAt(LocalDateTime.now());
            doc.setStatus(DocumentStatus.READY);
            documentRepository.save(doc);

            storageService.store(fileName, pdfBytes);

            eventProducer.sendPdfGeneratedEvent(
                prescription.getPrescriptionId(),
                prescription.getPatientId(),
                prescription.getPatientEmail(),
                prescription.getDoctorId(),
                prescription.getHospitalId(),
                "SUCCESS"
            );

            log.info("PDF generated successfully for: {}", prescriptionId);

        } catch (Exception e) {
            log.error("Failed to generate PDF for: {}", prescriptionId, e);
            documentRepository.findByPrescriptionId(prescriptionId).ifPresent(doc -> {
                doc.setStatus(DocumentStatus.FAILED);
                documentRepository.save(doc);
            });
            throw new RuntimeException("PDF generation failed for: " + prescriptionId, e);
        }
    }

    @DltHandler
    public void dltPdfGeneration(PrescriptionPdfTaskEvent event) {
        String prescriptionId = event.getPrescriptionId();
        log.error("DLQ message for prescription: {}. All retries exhausted", prescriptionId);
        documentRepository.findByPrescriptionId(prescriptionId).ifPresent(doc -> {
            doc.setStatus(DocumentStatus.FAILED);
            documentRepository.save(doc);
        });
        eventProducer.sendPdfGeneratedEvent(prescriptionId,"","","", "","FAILED");
    }
}
