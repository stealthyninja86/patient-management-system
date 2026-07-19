package com.pms.notificationservice.service.adapter;

import com.pms.notificationservice.dto.event.AppointmentConfirmationNotification;
import com.pms.notificationservice.dto.event.AppointmentReminderNotification;
import com.pms.notificationservice.dto.event.ConsentOtpNotification;
import com.pms.notificationservice.dto.event.NotificationMessage;
import com.pms.notificationservice.dto.event.PrescriptionReadyNotification;
import com.pms.notificationservice.model.NotificationChannel;
import com.pms.notificationservice.model.NotificationType;
import com.pms.notificationservice.service.resolver.NameResolver;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

@Service
@ConditionalOnProperty(name = "notification.email.provider", havingValue = "smtp")
public class SmtpEmailChannel implements NotificationProvider {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailChannel.class);

    private final JavaMailSender mailSender;
    private final ITemplateEngine templateEngine;
    private final String fromAddress;
    private final RestClient restClient;
    private final NameResolver nameResolver;

    public SmtpEmailChannel(JavaMailSender mailSender, ITemplateEngine templateEngine,
                            @Value("${notification.email.from}") String fromAddress,
                            NameResolver nameResolver) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.fromAddress = fromAddress;
        this.nameResolver = nameResolver;
        this.restClient = RestClient.builder()
                .baseUrl("http://clinical-service:4010")
                .build();
    }

    @Override
    public NotificationChannel supportedChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void send(NotificationMessage notification) {
        String templateName = resolveTemplateName(notification.type());
        Context ctx = new Context();
        ctx.setVariable("recipient", notification.recipient());
        ctx.setVariable("type", notification.type().name());
        ctx.setVariable("message", extractMessage(notification));

        switch (notification) {
            case AppointmentConfirmationNotification n -> {
                ctx.setVariable("patientName", n.patientName());
                ctx.setVariable("doctorName", n.doctorName());
                ctx.setVariable("hospitalName", n.hospitalName());
                ctx.setVariable("appointmentDate", n.date());
                ctx.setVariable("startTime", n.startTime());
                ctx.setVariable("endTime", n.endTime());
                ctx.setVariable("appointmentId", n.appointmentId());
            }
            case AppointmentReminderNotification n -> {
                ctx.setVariable("patientName", n.patientName());
                ctx.setVariable("doctorName", n.doctorName());
                ctx.setVariable("hospitalName", n.hospitalName());
                ctx.setVariable("appointmentDate", n.date());
                ctx.setVariable("startTime", n.startTime());
                ctx.setVariable("endTime", n.endTime());
                ctx.setVariable("appointmentId", n.appointmentId());
            }
            case ConsentOtpNotification n -> {
                ctx.setVariable("code", n.code());
                ctx.setVariable("domainKey", n.domainKey());
                ctx.setVariable("otpType", n.otpType());
            }
            case PrescriptionReadyNotification n -> {
                ctx.setVariable("patientName", n.patientName());
                ctx.setVariable("doctorName", n.doctorName());
                ctx.setVariable("hospitalName", n.hospitalName());
                ctx.setVariable("prescriptionId", n.prescriptionId());
            }
        }

        String htmlContent = templateEngine.process(templateName, ctx);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(notification.recipient());
            helper.setSubject(subjectForType(notification.type()));
            helper.setText(extractMessage(notification), htmlContent);
            helper.addInline("logo", new ClassPathResource("static/orbit-circle.png"));

            if (notification instanceof PrescriptionReadyNotification n && n.prescriptionId() != null) {
                attachPrescriptionPdf(helper, n.prescriptionId());
            }

            mailSender.send(mimeMessage);
            log.info("Email sent to {} via SMTP, type={}", notification.recipient(), notification.type());
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String extractMessage(NotificationMessage notification) {
        return switch (notification) {
            case AppointmentConfirmationNotification n -> n.message();
            case AppointmentReminderNotification n -> n.message();
            case ConsentOtpNotification n -> n.message();
            case PrescriptionReadyNotification n -> n.message();
        };
    }

    private void attachPrescriptionPdf(MimeMessageHelper helper, String prescriptionId) {
        try {
            String token = nameResolver.getInternalToken();
            byte[] pdfBytes = restClient.get()
                    .uri("/prescriptions/{id}/pdf", prescriptionId)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(byte[].class);
            if (pdfBytes != null && pdfBytes.length > 0) {
                helper.addAttachment(prescriptionId + ".pdf",
                        new ByteArrayResource(pdfBytes), "application/pdf");
                log.debug("Attached prescription PDF: {}.pdf ({} bytes)", prescriptionId, pdfBytes.length);
            }
        } catch (Exception e) {
            log.warn("Failed to attach prescription PDF {}: {}", prescriptionId, e.getMessage());
        }
    }

    static String resolveTemplateName(NotificationType type) {
        return switch (type) {
            case APPOINTMENT_CONFIRMATION -> "email/appointment-confirmation";
            case APPOINTMENT_REMINDER -> "email/appointment-confirmation";
            case APPOINTMENT_BOOKING -> "email/appointment-booking-otp";
            case APPOINTMENT_START -> "email/appointment-start-otp";
            case CONSENT_OTP -> "email/consent-otp";
            case PRESCRIPTION_READY -> "email/prescription-ready";
        };
    }

    static String subjectForType(NotificationType type) {
        return switch (type) {
            case APPOINTMENT_CONFIRMATION -> "Appointment Confirmed";
            case APPOINTMENT_REMINDER -> "Appointment Reminder";
            case APPOINTMENT_BOOKING -> "Your Appointment Booking Code";
            case APPOINTMENT_START -> "Appointment Starting Soon";
            case CONSENT_OTP -> "Consent Verification Code";
            case PRESCRIPTION_READY -> "Prescription Ready";
        };
    }
}
