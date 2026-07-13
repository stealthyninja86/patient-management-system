package com.pms.notificationservice.service.template;

import com.pms.notificationservice.dto.event.OtpNotificationContext;
import com.pms.notificationservice.model.NotificationChannel;
import com.pms.notificationservice.model.NotificationType;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * NotificationMessageTemplate for appointment start OTP.
 * Generated when the doctor is ready to begin — patient reads the code
 * aloud at the clinic. SMS is the primary channel since the patient
 * is physically present.
 */
@Component
public class AppointmentStartOtpTemplate
        extends NotificationMessageTemplate<OtpNotificationContext> {

    @Override
    public NotificationType getNotificationType() {
        return NotificationType.APPOINTMENT_START;
    }

    @Override
    public String getPatientId(OtpNotificationContext ctx) {
        return "";
    }

    @Override
    public String buildMessage(OtpNotificationContext ctx, NotificationChannel channel) {
        if (channel == NotificationChannel.SMS) {
            return String.format(
                    "Your appointment is starting. Show code: %s", ctx.code());
        }
        return String.format(
                """
                Dear Patient,
    
                Your appointment is about to start.
                Please share this code with your doctor: %s
    
                Valid for 3 minutes.
    
                — Orbit""", ctx.code());
    }

    @Override
    public String resolveRecipient(OtpNotificationContext ctx, NotificationChannel channel) {
        return channel == NotificationChannel.SMS ? ctx.phoneNumber() : ctx.email();
    }

    @Override
    public String buildDedupKey(OtpNotificationContext ctx, NotificationChannel channel) {
        return "start-otp-" + ctx.domainKey() + ":" + channel.name().toLowerCase();
    }

    @Override
    protected Map<String, Object> buildAttributes(OtpNotificationContext ctx, NotificationChannel channel) {
        if (channel != NotificationChannel.EMAIL) return Map.of();
        return Map.of(
            "code", ctx.code(),
            "domainKey", ctx.domainKey(),
            "type", "start"
        );
    }
}