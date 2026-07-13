package com.pms.notificationservice.service.template;

import com.pms.notificationservice.dto.event.AppointmentEventDTO;
import com.pms.notificationservice.model.NotificationChannel;
import com.pms.notificationservice.model.NotificationType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AppointmentConfirmationTemplate extends NotificationMessageTemplate<AppointmentEventDTO> {

    @Override
    public NotificationType getNotificationType() {
        return NotificationType.APPOINTMENT_CONFIRMATION;
    }

    @Override
    public String getPatientId(AppointmentEventDTO event) {
        return event.patientId();
    }

    @Override
    public String buildMessage(AppointmentEventDTO event, NotificationChannel channel) {
        String date = event.appointmentDate() != null ? event.appointmentDate() : "the scheduled date";
        if (channel == NotificationChannel.SMS) {
            return String.format(
                    "Appointment confirmed with %s on %s.",
                    event.doctorName(), date);
        }
        String hospital = event.hospitalName() != null ? event.hospitalName() : "our facility";
        return String.format(
                "Dear %s,\n\nYour appointment with %s at %s has been confirmed.\n" +
                "Date & Time: %s\n\nThank you,\nPatient Management System",
                event.patientName(), event.doctorName(),
                hospital, date);
    }

    @Override
    public String resolveRecipient(AppointmentEventDTO event, NotificationChannel channel) {
        return channel == NotificationChannel.SMS
                ? (event.patientPhone() != null ? event.patientPhone() : event.patientEmail())
                : event.patientEmail();
    }

    @Override
    public String buildDedupKey(AppointmentEventDTO event, NotificationChannel channel) {
        return "appt-" + event.appointmentId() + ":" + channel.name().toLowerCase();
    }

    @Override
    protected Map<String, Object> buildAttributes(AppointmentEventDTO event, NotificationChannel channel) {
        if (channel != NotificationChannel.EMAIL) return Map.of();
        return Map.of(
            "patientName", event.patientName() != null ? event.patientName() : "Patient",
            "doctorName", event.doctorName() != null ? event.doctorName() : "your doctor",
            "hospitalName", event.hospitalName() != null ? event.hospitalName() : "our facility",
            "appointmentDate", event.appointmentDate() != null ? event.appointmentDate() : "the scheduled date",
            "appointmentId", event.appointmentId()
        );
    }
}
