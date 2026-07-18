package com.pms.notificationservice.service.template;

import com.pms.notificationservice.dto.event.ConsentOtpNotification;
import com.pms.notificationservice.dto.event.NotificationMessage;
import com.pms.notificationservice.dto.event.OtpNotificationContext;
import com.pms.notificationservice.model.NotificationChannel;
import com.pms.notificationservice.model.NotificationType;
import org.springframework.stereotype.Component;

@Component
public class ConsentOtpMessageTemplate
        extends NotificationMessageTemplate<OtpNotificationContext> {

    @Override
    public NotificationMessage createRequest(OtpNotificationContext ctx, NotificationChannel channel) {
        return new ConsentOtpNotification(
                buildDedupKey(ctx, channel),
                getPatientId(ctx),
                NotificationType.CONSENT_OTP,
                channel,
                resolveRecipient(ctx, channel),
                buildMessage(ctx, channel),
                ctx.code(),
                ctx.domainKey(),
                "consent"
        );
    }

    @Override
    public NotificationType getNotificationType() {
        return NotificationType.CONSENT_OTP;
    }

    @Override
    public String getPatientId(OtpNotificationContext ctx) {
        return "";
    }

    @Override
    public String buildMessage(OtpNotificationContext ctx, NotificationChannel channel) {
        if (channel == NotificationChannel.SMS) {
            return String.format(
                    "Your consent verification code: %s. Valid 3 min.", ctx.code());
        }
        return String.format(
                """
                Dear Patient,
    
                A doctor has requested access to your medical records.
                Your verification code is: %s.
    
                This code is valid for 3 minutes. Do not share it with anyone.
                If you did not request this, please ignore.
    
                — Orbit""", ctx.code());
    }

    @Override
    public String resolveRecipient(OtpNotificationContext ctx, NotificationChannel channel) {
        return channel == NotificationChannel.SMS ? ctx.phoneNumber() : ctx.email();
    }

    @Override
    public String buildDedupKey(OtpNotificationContext ctx, NotificationChannel channel) {
        return "consent-otp-" + ctx.domainKey() + ":" + channel.name().toLowerCase();
    }
}
