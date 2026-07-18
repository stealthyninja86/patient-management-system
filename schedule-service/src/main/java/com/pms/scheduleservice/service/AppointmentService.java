package com.pms.scheduleservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.scheduleservice.dto.event.AppointmentEventDTO;
import com.pms.scheduleservice.dto.request.AppointmentRequestDTO;
import com.pms.scheduleservice.dto.response.AppointmentResponseDTO;
import com.pms.scheduleservice.dto.response.DoctorPatientDTO;
import com.pms.scheduleservice.exception.PatientNotFoundException;
import com.pms.scheduleservice.grpc.PatientGrpcClient;
import com.pms.scheduleservice.exception.AppointmentNotFoundException;
import com.pms.scheduleservice.exception.InvalidAppointmentOperationException;
import com.pms.scheduleservice.exception.TimeSlotNotAvailableException;
import com.pms.scheduleservice.exception.TimeSlotNotFoundException;
import com.pms.scheduleservice.service.mapper.AppointmentMapper;
import com.pms.scheduleservice.model.Appointment;
import com.pms.scheduleservice.model.AppointmentStatus;
import com.pms.scheduleservice.model.OutboxEvent;
import com.pms.scheduleservice.model.TimeSlot;
import com.pms.scheduleservice.repository.AppointmentRepository;
import com.pms.scheduleservice.repository.OutboxRepository;
import com.pms.scheduleservice.repository.TimeSlotRepository;
import com.pms.scheduleservice.service.util.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import patient.PatientResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final IdGenerator idGenerator;
    private final AppointmentMapper appointmentMapper;
    private final PatientGrpcClient patientGrpcClient;

    public AppointmentService(AppointmentRepository appointmentRepository,
                               TimeSlotRepository timeSlotRepository,
                               OutboxRepository outboxRepository,
                               ObjectMapper objectMapper,
                               IdGenerator idGenerator,
                               AppointmentMapper appointmentMapper,
                               PatientGrpcClient patientGrpcClient) {
        this.appointmentRepository = appointmentRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.idGenerator = idGenerator;
        this.appointmentMapper = appointmentMapper;
        this.patientGrpcClient = patientGrpcClient;
    }

    public List<AppointmentResponseDTO> getAllAppointments() {
        log.debug("Fetching all appointments");
        return appointmentRepository.findAll().stream()
                .map(appointmentMapper::toResponseDTO)
                .toList();
    }

    public AppointmentResponseDTO getAppointmentById(String appointmentId) {
        log.debug("Fetching appointment by id: {}", appointmentId);
        Appointment appointment = appointmentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found: " + appointmentId));
        return appointmentMapper.toResponseDTO(appointment);
    }

    public List<AppointmentResponseDTO> getAppointmentsByPatient(String patientId) {
        log.debug("Fetching appointments by patient: {}", patientId);
        return appointmentRepository.findByPatientId(patientId).stream()
                .map(appointmentMapper::toResponseDTO)
                .toList();
    }

    public List<AppointmentResponseDTO> getAppointmentsByDoctor(String doctorId) {
        log.debug("Fetching appointments by doctor: {}", doctorId);
        return appointmentRepository.findByDoctorId(doctorId).stream()
                .map(appointmentMapper::toResponseDTO)
                .toList();
    }

    public List<DoctorPatientDTO> getPatientsByDoctor(String doctorId) {
        log.debug("Fetching patients by doctor: {}", doctorId);
        return appointmentRepository.findByDoctorId(doctorId).stream()
                .map(a -> new DoctorPatientDTO(a.getPatientId(), a.getPatientName(), a.getPatientEmail()))
                .distinct()
                .toList();
    }

    public Appointment getAppointmentByIdEntity(String appointmentId) {
        log.debug("Fetching appointment entity by id: {}", appointmentId);
        return appointmentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found: " + appointmentId));
    }

    @Transactional
    public Appointment createAppointmentEntity(AppointmentRequestDTO request) {
        log.info("Creating appointment for patient: {}, timeSlot: {}", request.patientId(), request.timeSlotId());

        PatientResponse patientResponse;
        try {
            patientResponse = patientGrpcClient.getPatientById(request.patientId());
        } catch (Exception e) {
            throw new PatientNotFoundException("Patient not found with id: " + request.patientId());
        }

        TimeSlot timeSlot = timeSlotRepository.findByTimeSlotId(request.timeSlotId())
                .orElseThrow(() -> new TimeSlotNotFoundException("TimeSlot not found: " + request.timeSlotId()));

        boolean slotBooked = appointmentRepository.findByTimeSlotIdAndStatusIn(request.timeSlotId(),
                List.of(AppointmentStatus.PENDING_OTP, AppointmentStatus.BOOKED, AppointmentStatus.ONGOING)).isPresent();
        if (slotBooked) {
            throw new TimeSlotNotAvailableException("TimeSlot is not available: " + request.timeSlotId());
        }

        String appointmentId = idGenerator.nextId("APT", "appointment_seq");
        Appointment appointment = appointmentMapper.toEntity(request, appointmentId, timeSlot);
        appointment.setPatientName(patientResponse.getName());
        appointment.setPatientEmail(patientResponse.getEmail());
        appointment.setPatientPhone(patientResponse.getPhone());
        appointment.setStatus(AppointmentStatus.PENDING_OTP);
        appointment = appointmentRepository.save(appointment);

        return appointment;
    }

    @Transactional
    public AppointmentResponseDTO startAppointment(String appointmentId) {
        log.info("Starting appointment: {}", appointmentId);
        return updateAppointmentStatus(appointmentId, AppointmentStatus.ONGOING);
    }

    @Transactional
    public AppointmentResponseDTO completeAppointment(String appointmentId) {
        log.info("Completing appointment: {}", appointmentId);
        return updateAppointmentStatus(appointmentId, AppointmentStatus.COMPLETED);
    }

    @Transactional
    public AppointmentResponseDTO cancelAppointment(String appointmentId) {
        log.info("Cancelling appointment: {}", appointmentId);
        Appointment appointment = appointmentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found: " + appointmentId));

        validateTransition(appointment.getStatus(), AppointmentStatus.CANCELLED);

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment = appointmentRepository.save(appointment);

        writeOutboxEvent(appointment, "APPOINTMENT_CANCELLED", null);

        return appointmentMapper.toResponseDTO(appointment);
    }

    private String deriveEventType(AppointmentStatus status) {
        return switch (status) {
            case BOOKED -> "APPOINTMENT_BOOKED";
            case ONGOING -> "APPOINTMENT_ONGOING";
            case COMPLETED -> "APPOINTMENT_COMPLETED";
            case PENDING_OTP -> "APPOINTMENT_BOOKING_INITIATED";
            case CANCELLED -> "APPOINTMENT_CANCELLED";
        };
    }

    @Transactional
    public AppointmentResponseDTO rescheduleAppointment(String appointmentId, String newTimeSlotId) {
        log.info("Rescheduling appointment: {} to new timeSlot: {}", appointmentId, newTimeSlotId);
        Appointment appointment = appointmentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found: " + appointmentId));

        String oldTsId = appointment.getTimeSlotId();

        TimeSlot newTimeSlot = timeSlotRepository.findByTimeSlotId(newTimeSlotId)
                .orElseThrow(() -> new TimeSlotNotFoundException("TimeSlot not found: " + newTimeSlotId));

        boolean newSlotBooked = appointmentRepository.findByTimeSlotIdAndStatusIn(newTimeSlotId,
                List.of(AppointmentStatus.BOOKED, AppointmentStatus.ONGOING)).isPresent();
        if (newSlotBooked) {
            throw new TimeSlotNotAvailableException("New TimeSlot is not available: " + newTimeSlotId);
        }

        appointment.setTimeSlotId(newTimeSlotId);
        appointment = appointmentRepository.save(appointment);

        writeOutboxEvent(appointment, "APPOINTMENT_RESCHEDULED", null);

        return appointmentMapper.toResponseDTO(appointment);
    }

    @Transactional
    public void clearCancelledAppointment(String appointmentId) {
        log.debug("Clearing cancelled appointment: {}", appointmentId);
        Appointment appointment = appointmentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found: " + appointmentId));

        if (appointment.getStatus() != AppointmentStatus.CANCELLED) {
            throw new InvalidAppointmentOperationException("Only cancelled appointments can be cleared");
        }

        appointmentRepository.delete(appointment);
    }

    @Transactional
    public void deleteAppointment(String appointmentId) {
        log.debug("Deleting appointment: {}", appointmentId);
        Appointment appointment = appointmentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found: " + appointmentId));
        appointmentRepository.delete(appointment);
    }

    @Transactional
    public AppointmentResponseDTO updateAppointmentStatus(String appointmentId, AppointmentStatus newStatus) {
        log.debug("Updating appointment status: {} to {}", appointmentId, newStatus);
        Appointment appointment = appointmentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found: " + appointmentId));

        validateTransition(appointment.getStatus(), newStatus);

        appointment.setStatus(newStatus);
        if (newStatus == AppointmentStatus.COMPLETED) {
            appointment.setAppointmentCompleted(true);
        }
        appointment = appointmentRepository.save(appointment);

        writeOutboxEvent(appointment, deriveEventType(newStatus), null);

        return appointmentMapper.toResponseDTO(appointment);
    }

    private void writeOutboxEvent(Appointment appointment, String eventType, String appointmentDate) {
        try {
            AppointmentEventDTO dto = appointmentMapper.toEventDTO(appointment, eventType,
                    appointment.getPatientPhone(),
                    appointment.getHospitalId(),
                    appointment.getHospitalName(),
                    appointmentDate);
            String payload = objectMapper.writeValueAsString(dto);
            OutboxEvent outboxEvent = new OutboxEvent(
                UUID.randomUUID(),
                "APPOINTMENT",
                appointment.getAppointmentId(),
                eventType,
                "appointment-events",
                payload,
                appointment.getAppointmentId(),
                false,
                LocalDateTime.now(),
                null
            );
            outboxRepository.save(outboxEvent);
            log.debug("Outbox event saved: {} for appointment: {}", eventType, appointment.getAppointmentId());
        } catch (Exception e) {
            log.error("Failed to write outbox event for appointment: {}", appointment.getAppointmentId(), e);
        }
    }

    @Transactional
    public void transitionStatus(String appointmentId, AppointmentStatus newStatus) {
        log.debug("Transitioning appointment status: {} to {}", appointmentId, newStatus);
        Appointment appointment = appointmentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found: " + appointmentId));

        validateTransition(appointment.getStatus(), newStatus);

        appointment.setStatus(newStatus);
        appointmentRepository.save(appointment);
    }

    private void validateTransition(AppointmentStatus current, AppointmentStatus next) {
        if (current == next) {
            throw new InvalidAppointmentOperationException(
                "Appointment is already " + current.name().toLowerCase());
        }
        boolean valid = switch (current) {
            case PENDING_OTP -> next == AppointmentStatus.BOOKED || next == AppointmentStatus.CANCELLED;
            case BOOKED -> next == AppointmentStatus.ONGOING || next == AppointmentStatus.CANCELLED;
            case ONGOING -> next == AppointmentStatus.COMPLETED || next == AppointmentStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
        if (!valid) {
            throw new InvalidAppointmentOperationException(
                "Cannot transition appointment from " + current + " to " + next);
        }
    }
}
