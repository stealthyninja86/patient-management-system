package com.pms.notificationservice.dto.event;

import com.pms.notificationservice.model.NotificationChannel;
import com.pms.notificationservice.model.NotificationType;

public sealed interface NotificationMessage
        permits AppointmentConfirmationNotification, AppointmentReminderNotification,
                ConsentOtpNotification, PrescriptionReadyNotification,
                UserOnboardingNotification {

    String eventId();
    String patientId();
    NotificationType type();
    NotificationChannel channel();
    String recipient();
}
