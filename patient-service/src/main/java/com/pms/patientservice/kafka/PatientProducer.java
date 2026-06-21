package com.pms.patientservice.kafka;


import com.pms.patientservice.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import patient.PatientEvent;

@Service
public class PatientProducer {

    private static final Logger log = LoggerFactory.getLogger(PatientProducer.class);
    //create a kafka template with key: string, value: byte array(to keep the size down)
    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public PatientProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEvent(Patient patient) {
        PatientEvent event = PatientEvent.newBuilder()
                .setPatientId(patient.getPatientId())
                .setName(patient.getName())
                .setEmail(patient.getEmail())
                .setEventType("PATIENT_CREATED")
                .build();

        log.info("Sending Patient Event to Kafka Producer");
        kafkaTemplate.send("patient", event.toByteArray())
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to send patient event: {}", event, ex);
                } else {
                    log.info("Successfully sent patient event to topic {} partition {} offset {}", 
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                }
            });
    }
}
