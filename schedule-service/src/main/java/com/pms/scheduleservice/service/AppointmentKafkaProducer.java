package com.pms.scheduleservice.service;

import com.pms.scheduleservice.service.factory.AppointmentAssembler;
import com.pms.scheduleservice.model.Appointment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import patient.PatientResponse;

@Service
public class AppointmentKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(AppointmentKafkaProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final AppointmentAssembler appointmentFactory;

    public AppointmentKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate,
                                     AppointmentAssembler appointmentFactory) {
        this.kafkaTemplate = kafkaTemplate;
        this.appointmentFactory = appointmentFactory;
    }

    public void sendAppointmentBookedEvent(Appointment appointment, PatientResponse patientResponse, String appointmentDate) {
        log.info("Sending appointment booked event for appointmentId: {}", appointment.getAppointmentId());
        var event = appointmentFactory.toEventDTO(appointment, "APPOINTMENT_BOOKED",
            patientResponse.getPhone(), null, null, appointmentDate);
        kafkaTemplate.send("appointment-events", event);
    }

    public void sendAppointmentStatusChangedEvent(Appointment appointment, PatientResponse patientResponse) {
        log.info("Sending appointment status changed event for appointmentId: {}", appointment.getAppointmentId());
        var event = appointmentFactory.toEventDTO(appointment, "APPOINTMENT_STATUS_CHANGED",
            patientResponse != null ? patientResponse.getPhone() : null, null, null, null);
        kafkaTemplate.send("appointment-events", event);
    }
}
