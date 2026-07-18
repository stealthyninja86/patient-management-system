package com.pms.notificationservice.service.template;

import com.pms.notificationservice.dto.event.AppointmentConfirmationNotification;
import com.pms.notificationservice.dto.event.AppointmentEventDTO;
import com.pms.notificationservice.dto.event.NotificationMessage;
import com.pms.notificationservice.model.NotificationChannel;
import com.pms.notificationservice.model.NotificationType;
import org.springframework.stereotype.Component;

@Component
public class AppointmentConfirmationTemplate extends NotificationMessageTemplate<AppointmentEventDTO> {

    @Override
    public NotificationMessage createRequest(AppointmentEventDTO event, NotificationChannel channel) {
        if (event.appointmentId() == null || event.appointmentId().isBlank())
            throw new IllegalArgumentException("appointmentId is required");
        if (event.patientName() == null || event.patientName().isBlank())
            throw new IllegalArgumentException("patientName is required");
        if (event.doctorName() == null || event.doctorName().isBlank())
            throw new IllegalArgumentException("doctorName is required");
        if (event.hospitalName() == null || event.hospitalName().isBlank())
            throw new IllegalArgumentException("hospitalName is required");
        if (event.appointmentDate() == null || event.appointmentDate().isBlank())
            throw new IllegalArgumentException("appointmentDate is required");
        if (event.startTime() == null || event.startTime().isBlank())
            throw new IllegalArgumentException("startTime is required");
        if (event.endTime() == null || event.endTime().isBlank())
            throw new IllegalArgumentException("endTime is required");
        return new AppointmentConfirmationNotification(
                buildDedupKey(event, channel),
                event.patientId(),
                NotificationType.APPOINTMENT_CONFIRMATION,
                channel,
                resolveRecipient(event, channel),
                buildMessage(event, channel),
                event.patientName(),
                event.doctorName(),
                event.hospitalName(),
                event.appointmentDate(),
                event.startTime(),
                event.endTime(),
                event.appointmentId()
        );
    }

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
        String date = event.appointmentDate();
        String time = event.startTime() + " - " + event.endTime();
        if (channel == NotificationChannel.SMS) {
            return String.format(
                    "Appointment confirmed with %s on %s at %s.",
                    event.doctorName(), date, time);
        }
        return String.format(
                "Dear %s,\n\nYour appointment with %s at %s has been confirmed.\n" +
                "Date & Time: %s %s\n\nThank you,\nPatient Management System",
                event.patientName(), event.doctorName(),
                event.hospitalName(), date, time);
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
}
