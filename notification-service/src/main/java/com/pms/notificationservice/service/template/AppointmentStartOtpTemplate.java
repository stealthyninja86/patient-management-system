package com.pms.notificationservice.service.template;

import com.pms.notificationservice.dto.event.ConsentOtpNotification;
import com.pms.notificationservice.dto.event.NotificationMessage;
import com.pms.notificationservice.dto.event.OtpNotificationContext;
import com.pms.notificationservice.model.NotificationChannel;
import com.pms.notificationservice.model.NotificationType;
import org.springframework.stereotype.Component;

@Component
public class AppointmentStartOtpTemplate
        extends NotificationMessageTemplate<OtpNotificationContext> {

    @Override
    public NotificationMessage createRequest(OtpNotificationContext ctx, NotificationChannel channel) {
        return new ConsentOtpNotification(
                buildDedupKey(ctx, channel),
                getPatientId(ctx),
                NotificationType.APPOINTMENT_START,
                channel,
                resolveRecipient(ctx, channel),
                buildMessage(ctx, channel),
                ctx.code(),
                ctx.domainKey(),
                "start"
        );
    }

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
}
