package com.pms.scheduleservice.service;

import com.pms.scheduleservice.dto.request.AppointmentRequestDTO;
import com.pms.scheduleservice.dto.response.AppointmentResponseDTO;
import com.pms.scheduleservice.dto.response.DoctorPatientDTO;
import com.pms.scheduleservice.exception.PatientNotFoundException;
import com.pms.scheduleservice.grpc.PatientGrpcClient;
import com.pms.scheduleservice.exception.AppointmentNotFoundException;
import com.pms.scheduleservice.exception.InvalidAppointmentOperationException;
import com.pms.scheduleservice.exception.TimeSlotNotAvailableException;
import com.pms.scheduleservice.exception.TimeSlotNotFoundException;
import com.pms.scheduleservice.service.factory.AppointmentAssembler;
import com.pms.scheduleservice.model.Appointment;
import com.pms.scheduleservice.model.AppointmentStatus;
import com.pms.scheduleservice.model.TimeSlot;
import com.pms.scheduleservice.repository.AppointmentRepository;
import com.pms.scheduleservice.repository.TimeSlotRepository;
import com.pms.scheduleservice.service.util.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final AppointmentKafkaProducer kafkaProducer;
    private final IdGenerator idGenerator;
    private final AppointmentAssembler appointmentFactory;
    private final PatientGrpcClient patientGrpcClient;

    public AppointmentService(AppointmentRepository appointmentRepository,
                               TimeSlotRepository timeSlotRepository,
                               AppointmentKafkaProducer kafkaProducer,
                               IdGenerator idGenerator,
                               AppointmentAssembler appointmentFactory,
                               PatientGrpcClient patientGrpcClient) {
        this.appointmentRepository = appointmentRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.kafkaProducer = kafkaProducer;
        this.idGenerator = idGenerator;
        this.appointmentFactory = appointmentFactory;
        this.patientGrpcClient = patientGrpcClient;
    }

    public List<AppointmentResponseDTO> getAllAppointments() {
        log.debug("Fetching all appointments");
        return appointmentRepository.findAll().stream()
                .map(appointmentFactory::toResponseDTO)
                .toList();
    }

    public AppointmentResponseDTO getAppointmentById(String appointmentId) {
        log.debug("Fetching appointment by id: {}", appointmentId);
        Appointment appointment = appointmentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found: " + appointmentId));
        return appointmentFactory.toResponseDTO(appointment);
    }

    public List<AppointmentResponseDTO> getAppointmentsByPatient(String patientId) {
        log.debug("Fetching appointments by patient: {}", patientId);
        return appointmentRepository.findByPatientId(patientId).stream()
                .map(appointmentFactory::toResponseDTO)
                .toList();
    }

    public List<AppointmentResponseDTO> getAppointmentsByDoctor(String doctorId) {
        log.debug("Fetching appointments by doctor: {}", doctorId);
        return appointmentRepository.findByDoctorId(doctorId).stream()
                .map(appointmentFactory::toResponseDTO)
                .toList();
    }

    public List<DoctorPatientDTO> getPatientsByDoctor(String doctorId) {
        log.debug("Fetching patients by doctor: {}", doctorId);
        return appointmentRepository.findByDoctorId(doctorId).stream()
                .map(a -> new DoctorPatientDTO(a.getPatientId(), a.getPatientName(), a.getPatientEmail()))
                .distinct()
                .toList();
    }

    @Transactional
    public AppointmentResponseDTO createAppointment(AppointmentRequestDTO request) {
        log.info("Creating appointment for patient: {}, timeSlot: {}", request.patientId(), request.timeSlotId());

        patient.PatientResponse patientResponse;
        try {
            patientResponse = patientGrpcClient.getPatientById(request.patientId());
        } catch (Exception e) {
            throw new PatientNotFoundException("Patient not found with id: " + request.patientId());
        }

        TimeSlot timeSlot = timeSlotRepository.findByTimeSlotId(request.timeSlotId())
                .orElseThrow(() -> new TimeSlotNotFoundException("TimeSlot not found: " + request.timeSlotId()));

        boolean slotBooked = appointmentRepository.findByTimeSlotIdAndStatusIn(request.timeSlotId(),
                List.of(AppointmentStatus.BOOKED, AppointmentStatus.ONGOING)).isPresent();
        if (slotBooked) {
            throw new TimeSlotNotAvailableException("TimeSlot is not available: " + request.timeSlotId());
        }

        String appointmentId = idGenerator.nextId("APT", "appointment_seq");
        Appointment appointment = appointmentFactory.toEntity(request, appointmentId, timeSlot);
        appointment.setPatientName(patientResponse.getName());
        appointment.setPatientEmail(patientResponse.getEmail());
        appointment = appointmentRepository.save(appointment);

        kafkaProducer.sendAppointmentBookedEvent(appointment, patientResponse,
            timeSlot.getStartTime().toLocalDate().toString());

        return appointmentFactory.toResponseDTO(appointment);
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

        kafkaProducer.sendAppointmentStatusChangedEvent(appointment, null);

        return appointmentFactory.toResponseDTO(appointment);
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

        kafkaProducer.sendAppointmentStatusChangedEvent(appointment, null);

        return appointmentFactory.toResponseDTO(appointment);
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

        kafkaProducer.sendAppointmentStatusChangedEvent(appointment, null);

        return appointmentFactory.toResponseDTO(appointment);
    }

    private void validateTransition(AppointmentStatus current, AppointmentStatus next) {
        if (current == next) {
            throw new InvalidAppointmentOperationException(
                "Appointment is already " + current.name().toLowerCase());
        }
        boolean valid = switch (current) {
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
