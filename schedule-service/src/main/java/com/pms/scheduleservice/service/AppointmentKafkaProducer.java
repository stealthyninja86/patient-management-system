package com.pms.scheduleservice.service;

import com.pms.scheduleservice.factory.AppointmentFactory;
import com.pms.scheduleservice.model.Appointment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AppointmentKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(AppointmentKafkaProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final AppointmentFactory appointmentFactory;

    public AppointmentKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate,
                                     AppointmentFactory appointmentFactory) {
        this.kafkaTemplate = kafkaTemplate;
        this.appointmentFactory = appointmentFactory;
    }

    public void sendAppointmentBookedEvent(Appointment appointment) {
        log.info("Sending appointment booked event for appointmentId: {}", appointment.getAppointmentId());
        var event = appointmentFactory.toEventDTO(appointment, "APPOINTMENT_BOOKED");
        kafkaTemplate.send("appointment-events", event);
    }

    public void sendAppointmentStatusChangedEvent(Appointment appointment) {
        log.info("Sending appointment status changed event for appointmentId: {}", appointment.getAppointmentId());
        var event = appointmentFactory.toEventDTO(appointment, "APPOINTMENT_STATUS_CHANGED");
        kafkaTemplate.send("appointment-events", event);
    }
}
