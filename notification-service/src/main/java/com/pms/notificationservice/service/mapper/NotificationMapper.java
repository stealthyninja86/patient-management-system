package com.pms.notificationservice.service.mapper;

import com.pms.notificationservice.dto.event.AppointmentConfirmationNotification;
import com.pms.notificationservice.dto.event.AppointmentReminderNotification;
import com.pms.notificationservice.dto.event.ConsentOtpNotification;

import com.pms.notificationservice.dto.event.NotificationMessage;
import com.pms.notificationservice.dto.event.PrescriptionReadyNotification;
import com.pms.notificationservice.dto.event.UserOnboardingNotification;
import com.pms.notificationservice.dto.request.NotificationRequest;
import com.pms.notificationservice.dto.response.NotificationResponseDTO;
import com.pms.notificationservice.model.Notification;
import com.pms.notificationservice.model.NotificationStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class NotificationMapper {

    public Notification createNotification(NotificationMessage message) {
        return Notification.builder()
                .patientId(message.patientId())
                .type(message.type())
                .channel(message.channel())
                .recipient(message.recipient())
                .message(extractMessage(message))
                .status(NotificationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private String extractMessage(NotificationMessage message) {
        return switch (message) {
            case AppointmentConfirmationNotification n -> n.message();
            case AppointmentReminderNotification n -> n.message();
            case ConsentOtpNotification n -> n.message();
            case PrescriptionReadyNotification n -> n.message();
            case UserOnboardingNotification n -> n.message();
        };
    }

    public NotificationMessage toMessage(NotificationRequest request) {
        return switch (request.type()) {
            case APPOINTMENT_CONFIRMATION -> new AppointmentConfirmationNotification(
                    request.eventId(), request.patientId(), request.type(),
                    request.channel(), request.recipient(), request.message(),
                    request.patientName(), request.doctorName(),
                    request.hospitalName(), request.date(),
                    request.startTime(), request.endTime(),
                    request.appointmentId());
            case APPOINTMENT_REMINDER -> new AppointmentReminderNotification(
                    request.eventId(), request.patientId(), request.type(),
                    request.channel(), request.recipient(), request.message(),
                    request.patientName(), request.doctorName(),
                    request.hospitalName(), request.date(),
                    request.startTime(), request.endTime(),
                    request.appointmentId());
            case PRESCRIPTION_READY -> new PrescriptionReadyNotification(
                    request.eventId(), request.patientId(), "",
                    "", "", "", request.type(),
                    request.channel(), request.recipient(), request.message());
            case CONSENT_OTP, APPOINTMENT_BOOKING, APPOINTMENT_START -> new ConsentOtpNotification(
                    request.eventId(), request.patientId(), request.type(),
                    request.channel(), request.recipient(), request.message(),
                    "", "", "");
            case USER_ONBOARDING -> new UserOnboardingNotification(
                    request.eventId(), request.patientId(), request.recipient(),
                    request.patientName(), "", request.type(),
                    request.channel(), request.recipient(), request.message());
        };
    }

    public NotificationResponseDTO toResponse(Notification notification) {
        return new NotificationResponseDTO(
            notification.getId(),
            notification.getPatientId(),
            notification.getType(),
            notification.getChannel(),
            notification.getRecipient(),
            notification.getMessage(),
            notification.getStatus(),
            notification.getRetryCount(),
            notification.getErrorMessage(),
            notification.getCreatedAt(),
            notification.getSentAt()
        );
    }
}
