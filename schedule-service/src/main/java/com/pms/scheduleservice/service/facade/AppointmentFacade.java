package com.pms.scheduleservice.service.facade;

import com.pms.scheduleservice.dto.request.AppointmentRequestDTO;
import com.pms.scheduleservice.dto.response.AppointmentResponseDTO;
import com.pms.scheduleservice.dto.response.DoctorPatientDTO;
import com.pms.scheduleservice.grpc.OtpGrpcClient;
import com.pms.scheduleservice.model.Appointment;
import com.pms.scheduleservice.model.AppointmentStatus;
import com.pms.scheduleservice.service.AppointmentService;
import notification.OptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class AppointmentFacade {

    private static final Logger log = LoggerFactory.getLogger(AppointmentFacade.class);

    private final AppointmentService appointmentService;
    private final OtpGrpcClient otpGrpcClient;

    public AppointmentFacade(AppointmentService appointmentService,
                             OtpGrpcClient otpGrpcClient) {
        this.appointmentService = appointmentService;
        this.otpGrpcClient = otpGrpcClient;
    }

    public List<AppointmentResponseDTO> getAllAppointments() {
        log.debug("Fetching all appointments via facade");
        return appointmentService.getAllAppointments();
    }

    public AppointmentResponseDTO getAppointmentById(String appointmentId) {
        log.debug("Fetching appointment by id via facade: {}", appointmentId);
        return appointmentService.getAppointmentById(appointmentId);
    }

    public List<AppointmentResponseDTO> getAppointmentsByPatient(String patientId) {
        log.debug("Fetching appointments by patient via facade: {}", patientId);
        return appointmentService.getAppointmentsByPatient(patientId);
    }

    public List<AppointmentResponseDTO> getAppointmentsByDoctor(String doctorId) {
        log.debug("Fetching appointments by doctor via facade: {}", doctorId);
        return appointmentService.getAppointmentsByDoctor(doctorId);
    }

    public List<DoctorPatientDTO> getPatientsByDoctor(String doctorId) {
        log.debug("Fetching patients by doctor via facade: {}", doctorId);
        return appointmentService.getPatientsByDoctor(doctorId);
    }

    public AppointmentResponseDTO startAppointment(String appointmentId) {
        log.debug("Starting appointment via facade: {}", appointmentId);
        return appointmentService.startAppointment(appointmentId);
    }

    public AppointmentResponseDTO completeAppointment(String appointmentId) {
        log.debug("Completing appointment via facade: {}", appointmentId);
        return appointmentService.completeAppointment(appointmentId);
    }

    public AppointmentResponseDTO cancelAppointment(String appointmentId) {
        log.debug("Cancelling appointment via facade: {}", appointmentId);
        return appointmentService.cancelAppointment(appointmentId);
    }

    public AppointmentResponseDTO rescheduleAppointment(String appointmentId, String newTimeSlotId) {
        log.debug("Rescheduling appointment via facade: {} to timeSlot: {}", appointmentId, newTimeSlotId);
        return appointmentService.rescheduleAppointment(appointmentId, newTimeSlotId);
    }

    public void clearCancelledAppointment(String appointmentId) {
        log.debug("Clearing cancelled appointment via facade: {}", appointmentId);
        appointmentService.clearCancelledAppointment(appointmentId);
    }

    public void deleteAppointment(String appointmentId) {
        log.debug("Deleting appointment via facade: {}", appointmentId);
        appointmentService.deleteAppointment(appointmentId);
    }

    @Transactional
    public String initBooking(AppointmentRequestDTO request) {
        log.debug("Initiating booking via facade for patient: {}, timeSlot: {}", request.patientId(), request.timeSlotId());
        Appointment appointment = appointmentService.createAppointmentEntity(request);

        OptService.GenerateOtpRequest otpRequest = OptService.GenerateOtpRequest.newBuilder()
                .setDomainKey("booking:" + appointment.getAppointmentId())
                .setPhoneNumber(appointment.getPatientPhone())
                .setEmail(appointment.getPatientEmail())
                .setNotificationType("APPOINTMENT_BOOKING")
                .build();

        otpGrpcClient.generateOtp(otpRequest);

        return appointment.getAppointmentId();
    }

    public AppointmentResponseDTO confirmBooking(String appointmentId, String code) {
        log.debug("Confirming booking via facade for appointment: {}", appointmentId);

        OptService.VerifyOtpRequest verifyRequest = OptService.VerifyOtpRequest.newBuilder()
                .setDomainKey("booking:" + appointmentId)
                .setCode(code)
                .build();

        OptService.VerifyOtpResponse verifyResponse = otpGrpcClient.verifyOtp(verifyRequest);
        boolean verified = verifyResponse.getVerified();
        String status = verifyResponse.getStatus();

        if (!verified) {
            if ("LOCKED".equals(status) || "EXPIRED".equals(status)) {
                return appointmentService.cancelAppointment(appointmentId);
            }
            return appointmentService.getAppointmentById(appointmentId);
        }

        return appointmentService.updateAppointmentStatus(appointmentId, AppointmentStatus.BOOKED);
    }

    public String initStart(String appointmentId) {
        log.debug("Initiating start via facade for appointment: {}", appointmentId);
        Appointment appointment = appointmentService.getAppointmentByIdEntity(appointmentId);

        OptService.GenerateOtpRequest otpRequest = OptService.GenerateOtpRequest.newBuilder()
                .setDomainKey("start:" + appointmentId)
                .setPhoneNumber(appointment.getPatientPhone())
                .setEmail(appointment.getPatientEmail())
                .setNotificationType("APPOINTMENT_START")
                .build();

        otpGrpcClient.generateOtp(otpRequest);

        return appointmentId;
    }

    public AppointmentResponseDTO confirmBookingDev(String appointmentId) {
        log.debug("Dev bypass: confirming booking for appointment: {} (no OTP)", appointmentId);
        return appointmentService.updateAppointmentStatus(appointmentId, AppointmentStatus.BOOKED);
    }

    public AppointmentResponseDTO confirmStartDev(String appointmentId) {
        log.debug("Dev bypass: starting appointment {} (no OTP)", appointmentId);
        return appointmentService.startAppointment(appointmentId);
    }

    public AppointmentResponseDTO confirmStart(String appointmentId, String code) {
        log.debug("Confirming start via facade for appointment: {}", appointmentId);

        OptService.VerifyOtpRequest verifyRequest = OptService.VerifyOtpRequest.newBuilder()
                .setDomainKey("start:" + appointmentId)
                .setCode(code)
                .build();

        OptService.VerifyOtpResponse verifyResponse = otpGrpcClient.verifyOtp(verifyRequest);
        boolean verified = verifyResponse.getVerified();
        String status = verifyResponse.getStatus();

        if (!verified) {
            if ("LOCKED".equals(status) || "EXPIRED".equals(status)) {
                return appointmentService.cancelAppointment(appointmentId);
            }
            return appointmentService.getAppointmentById(appointmentId);
        }

        return appointmentService.startAppointment(appointmentId);
    }
}
