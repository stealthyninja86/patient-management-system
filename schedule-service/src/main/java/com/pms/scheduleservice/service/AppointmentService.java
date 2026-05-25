package com.pms.scheduleservice.service;

import com.pms.scheduleservice.dto.AppointmentRequestDTO;
import com.pms.scheduleservice.dto.AppointmentResponseDTO;
import com.pms.scheduleservice.exception.AppointmentNotFoundException;
import com.pms.scheduleservice.exception.InvalidAppointmentOperationException;
import com.pms.scheduleservice.exception.TimeSlotNotAvailableException;
import com.pms.scheduleservice.exception.TimeSlotNotFoundException;
import com.pms.scheduleservice.factory.AppointmentFactory;
import com.pms.scheduleservice.model.Appointment;
import com.pms.scheduleservice.model.AppointmentStatus;
import com.pms.scheduleservice.model.TimeSlot;
import com.pms.scheduleservice.model.TimeSlotStatus;
import com.pms.scheduleservice.repository.AppointmentRepository;
import com.pms.scheduleservice.repository.TimeSlotRepository;
import com.pms.scheduleservice.util.IdGenerator;
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
    private final AppointmentFactory appointmentFactory;

    public AppointmentService(AppointmentRepository appointmentRepository,
                               TimeSlotRepository timeSlotRepository,
                               AppointmentKafkaProducer kafkaProducer,
                               IdGenerator idGenerator,
                               AppointmentFactory appointmentFactory) {
        this.appointmentRepository = appointmentRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.kafkaProducer = kafkaProducer;
        this.idGenerator = idGenerator;
        this.appointmentFactory = appointmentFactory;
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

    public List<AppointmentResponseDTO> getAppointmentsByHospital(String hospitalId) {
        log.debug("Fetching appointments by hospital: {}", hospitalId);
        return appointmentRepository.findByHospitalId(hospitalId).stream()
                .map(appointmentFactory::toResponseDTO)
                .toList();
    }

    @Transactional
    public AppointmentResponseDTO createAppointment(AppointmentRequestDTO request) {
        log.info("Creating appointment for patient: {}, timeSlot: {}", request.patientId(), request.timeSlotId());
        TimeSlot timeSlot = timeSlotRepository.findByTimeSlotId(request.timeSlotId())
                .orElseThrow(() -> new TimeSlotNotFoundException("TimeSlot not found: " + request.timeSlotId()));

        if (timeSlot.getStatus() != TimeSlotStatus.AVAILABLE) {
            throw new TimeSlotNotAvailableException("TimeSlot is not available: " + request.timeSlotId());
        }

        timeSlot.setStatus(TimeSlotStatus.BOOKED);
        timeSlotRepository.save(timeSlot);

        String appointmentId = idGenerator.nextId("APT", "appointment_seq");
        Appointment appointment = appointmentFactory.toEntity(request, appointmentId, timeSlot);
        appointment = appointmentRepository.save(appointment);

        kafkaProducer.sendAppointmentBookedEvent(appointment);

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

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new InvalidAppointmentOperationException("Appointment is already cancelled");
        }

        String tsId = appointment.getTimeSlotId();
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment = appointmentRepository.save(appointment);

        TimeSlot timeSlot = timeSlotRepository.findByTimeSlotId(tsId)
                .orElseThrow(() -> new TimeSlotNotFoundException("TimeSlot not found: " + tsId));
        timeSlot.setStatus(TimeSlotStatus.AVAILABLE);
        timeSlotRepository.save(timeSlot);

        kafkaProducer.sendAppointmentStatusChangedEvent(appointment);

        return appointmentFactory.toResponseDTO(appointment);
    }

    @Transactional
    public AppointmentResponseDTO rescheduleAppointment(String appointmentId, String newTimeSlotId) {
        log.info("Rescheduling appointment: {} to new timeSlot: {}", appointmentId, newTimeSlotId);
        Appointment appointment = appointmentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found: " + appointmentId));

        String oldTsId = appointment.getTimeSlotId();
        TimeSlot oldTimeSlot = timeSlotRepository.findByTimeSlotId(oldTsId)
                .orElseThrow(() -> new TimeSlotNotFoundException("TimeSlot not found: " + oldTsId));
        oldTimeSlot.setStatus(TimeSlotStatus.AVAILABLE);
        timeSlotRepository.save(oldTimeSlot);

        TimeSlot newTimeSlot = timeSlotRepository.findByTimeSlotId(newTimeSlotId)
                .orElseThrow(() -> new TimeSlotNotFoundException("TimeSlot not found: " + newTimeSlotId));

        if (newTimeSlot.getStatus() != TimeSlotStatus.AVAILABLE) {
            throw new TimeSlotNotAvailableException("New TimeSlot is not available: " + newTimeSlotId);
        }

        newTimeSlot.setStatus(TimeSlotStatus.BOOKED);
        timeSlotRepository.save(newTimeSlot);

        appointment.setTimeSlotId(newTimeSlotId);
        appointment = appointmentRepository.save(appointment);

        kafkaProducer.sendAppointmentStatusChangedEvent(appointment);

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

        String tsId = appointment.getTimeSlotId();
        appointment.setStatus(newStatus);
        appointment = appointmentRepository.save(appointment);

        TimeSlot timeSlot = timeSlotRepository.findByTimeSlotId(tsId)
                .orElseThrow(() -> new TimeSlotNotFoundException("TimeSlot not found: " + tsId));

        TimeSlotStatus timeSlotStatus = switch (newStatus) {
            case ONGOING -> TimeSlotStatus.ONGOING;
            case COMPLETED -> TimeSlotStatus.COMPLETED;
            case CANCELLED, NO_SHOW -> TimeSlotStatus.AVAILABLE;
            default -> timeSlot.getStatus();
        };
        timeSlot.setStatus(timeSlotStatus);
        timeSlotRepository.save(timeSlot);

        kafkaProducer.sendAppointmentStatusChangedEvent(appointment);

        return appointmentFactory.toResponseDTO(appointment);
    }
}
