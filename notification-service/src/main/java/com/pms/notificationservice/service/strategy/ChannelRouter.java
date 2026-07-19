package com.pms.notificationservice.service.strategy;

import com.pms.notificationservice.model.NotificationChannel;
import com.pms.notificationservice.model.NotificationType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ChannelRouter {

    private final Map<NotificationType, NotificationChannelStrategy> strategyMap;

    public ChannelRouter(AppointmentChannelStrategy appointmentChannelStrategy,
                         PrescriptionChannelStrategy prescriptionChannelStrategy,
                         OtpChannelStrategy otpChannelStrategy
    ) {
        strategyMap = new EnumMap<>(NotificationType.class);
        strategyMap.put(NotificationType.APPOINTMENT_CONFIRMATION, appointmentChannelStrategy);
        strategyMap.put(NotificationType.PRESCRIPTION_READY, prescriptionChannelStrategy);
        strategyMap.put(NotificationType.APPOINTMENT_BOOKING, otpChannelStrategy);
        strategyMap.put(NotificationType.APPOINTMENT_START, otpChannelStrategy);
        strategyMap.put(NotificationType.CONSENT_OTP, otpChannelStrategy);
        strategyMap.put(NotificationType.USER_ONBOARDING, appointmentChannelStrategy);
    }

    public List<NotificationChannel> resolve(NotificationType notificationType) {
        NotificationChannelStrategy stategy = strategyMap.get(notificationType);

        if(stategy == null) {
            throw new IllegalArgumentException("No Channel Strategy found for notificationType: " + notificationType);
        }
        return stategy.resolveChannels();
    }
}
