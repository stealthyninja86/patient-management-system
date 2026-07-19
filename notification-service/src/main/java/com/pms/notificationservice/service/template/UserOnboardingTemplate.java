package com.pms.notificationservice.service.template;

import com.pms.notificationservice.dto.event.NotificationMessage;
import com.pms.notificationservice.dto.event.UserOnboardingNotification;
import com.pms.notificationservice.dto.event.UserRegistrationEventDTO;
import com.pms.notificationservice.model.NotificationChannel;
import com.pms.notificationservice.model.NotificationType;
import org.springframework.stereotype.Component;

@Component
public class UserOnboardingTemplate extends NotificationMessageTemplate<UserRegistrationEventDTO> {

    @Override
    public NotificationMessage createRequest(UserRegistrationEventDTO event, NotificationChannel channel) {
        if (event.email() == null || event.email().isBlank())
            throw new IllegalArgumentException("email is required");
        return new UserOnboardingNotification(
                buildDedupKey(event, channel),
                event.email(),
                event.email(),
                event.email(),
                event.role(),
                NotificationType.USER_ONBOARDING,
                channel,
                resolveRecipient(event, channel),
                buildMessage(event, channel)
        );
    }

    @Override
    public NotificationType getNotificationType() {
        return NotificationType.USER_ONBOARDING;
    }

    @Override
    protected String getPatientId(UserRegistrationEventDTO event) {
        return event.email();
    }

    @Override
    protected String buildMessage(UserRegistrationEventDTO event, NotificationChannel channel) {
        if (channel == NotificationChannel.SMS) {
            return String.format(
                    "Welcome to Patient Management System! Your %s account is active.",
                    event.role());
        }
        return String.format(
                "Dear %s,\n\nWelcome to Patient Management System!\n\n" +
                "Your %s account has been created successfully. You can now log in " +
                "to manage appointments, view prescriptions, and more.\n\n" +
                "Thank you,\nPatient Management System Team",
                event.email(), event.role());
    }

    @Override
    protected String resolveRecipient(UserRegistrationEventDTO event, NotificationChannel channel) {
        return event.email();
    }

    @Override
    protected String buildDedupKey(UserRegistrationEventDTO event, NotificationChannel channel) {
        return "onboarding-" + event.email() + ":" + channel.name().toLowerCase();
    }
}
