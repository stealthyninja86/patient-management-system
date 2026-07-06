package com.pms.notificationservice.service.template;

import com.pms.notificationservice.dto.event.OtpNotificationContext;
import com.pms.notificationservice.model.NotificationChannel;
import com.pms.notificationservice.model.NotificationType;
import org.springframework.stereotype.Component;

@Component
public class AppointmentBookingOtpTemplate extends NotificationMessageTemplate<OtpNotificationContext> {
    @Override
    public NotificationType getNotificationType() {
        return NotificationType.APPOINTMENT_BOOKING;
    }

    @Override
    protected String getPatientId(OtpNotificationContext event) {
        return "";
    }

    @Override
    protected String buildMessage(OtpNotificationContext event, NotificationChannel channel) {
        if (channel == NotificationChannel.SMS){
            return String.format(
                    """
                            Your appointment Booking code: %s . Valid for 3 mins
                            - Orbit""", event.code()
            );
        }
        return String.format(
                """
                Dear Patient,
    
                Your appointment is pending confirmation.
                Booking code: %s
    
                Enter this code to confirm your appointment.
                Valid for 3 minutes.
    
                — Orbit""", event.code());
    }

    @Override
    protected String resolveRecipient(OtpNotificationContext event, NotificationChannel channel) {
        return channel == NotificationChannel.SMS ? event.phoneNumber() : event.email();
    }

    @Override
    protected String buildDedupKey(OtpNotificationContext event, NotificationChannel channel) {
        return "booking-otp-" + event.domainKey() + ":" + channel.name().toLowerCase();
    }
}
