package com.pms.scheduleservice.service;

import com.pms.scheduleservice.dto.TimeSlotRequestDTO;
import com.pms.scheduleservice.dto.TimeSlotResponseDTO;
import com.pms.scheduleservice.exception.AppointmentNotFoundException;
import com.pms.scheduleservice.exception.TimeSlotNotFoundException;
import com.pms.scheduleservice.factory.TimeSlotFactory;
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
import java.util.UUID;

@Service
public class TimeSlotService {

    private static final Logger log = LoggerFactory.getLogger(TimeSlotService.class);

    private final TimeSlotRepository timeSlotRepository;
    private final AppointmentRepository appointmentRepository;
    private final IdGenerator idGenerator;
    private final TimeSlotFactory timeSlotFactory;

    public TimeSlotService(TimeSlotRepository timeSlotRepository,
                            AppointmentRepository appointmentRepository,
                            IdGenerator idGenerator,
                            TimeSlotFactory timeSlotFactory) {
        this.timeSlotRepository = timeSlotRepository;
        this.appointmentRepository = appointmentRepository;
        this.idGenerator = idGenerator;
        this.timeSlotFactory = timeSlotFactory;
    }

    public List<TimeSlotResponseDTO> getAllTimeSlots() {
        log.debug("Fetching all time slots");
        return timeSlotRepository.findAll().stream()
                .map(timeSlotFactory::toResponseDTO)
                .toList();
    }

    public TimeSlotResponseDTO getTimeSlotById(String timeSlotId) {
        log.debug("Fetching time slot by id: {}", timeSlotId);
        TimeSlot timeSlot = timeSlotRepository.findByTimeSlotId(timeSlotId)
                .orElseThrow(() -> new TimeSlotNotFoundException("TimeSlot not found: " + timeSlotId));
        return timeSlotFactory.toResponseDTO(timeSlot);
    }

    public List<TimeSlotResponseDTO> getTimeSlotsByDoctor(String doctorId) {
        log.debug("Fetching time slots by doctor: {}", doctorId);
        return timeSlotRepository.findByDoctorId(doctorId).stream()
                .map(timeSlotFactory::toResponseDTO)
                .toList();
    }

    public List<TimeSlotResponseDTO> getAvailableTimeSlotsByDoctor(String doctorId) {
        log.debug("Fetching available time slots by doctor: {}", doctorId);
        return timeSlotRepository.findByDoctorIdAndStatus(doctorId, TimeSlotStatus.AVAILABLE).stream()
                .map(timeSlotFactory::toResponseDTO)
                .toList();
    }

    public List<TimeSlotResponseDTO> getTimeSlotsByHospital(String hospitalId) {
        log.debug("Fetching time slots by hospital: {}", hospitalId);
        return timeSlotRepository.findByHospitalId(hospitalId).stream()
                .map(timeSlotFactory::toResponseDTO)
                .toList();
    }

    @Transactional
    public TimeSlotResponseDTO createTimeSlot(TimeSlotRequestDTO request) {
        log.info("Creating time slot for doctor: {}", request.doctorId());
        String timeSlotId = idGenerator.nextId("TS", "time_slot_seq");
        TimeSlot timeSlot = timeSlotFactory.toEntity(request, timeSlotId);
        timeSlot = timeSlotRepository.save(timeSlot);
        return timeSlotFactory.toResponseDTO(timeSlot);
    }

    @Transactional
    public TimeSlotResponseDTO updateTimeSlotStatus(String timeSlotId, TimeSlotStatus status) {
        log.debug("Updating time slot status: {} to {}", timeSlotId, status);
        TimeSlot timeSlot = timeSlotRepository.findByTimeSlotId(timeSlotId)
                .orElseThrow(() -> new TimeSlotNotFoundException("TimeSlot not found: " + timeSlotId));
        timeSlot.setStatus(status);
        timeSlot = timeSlotRepository.save(timeSlot);
        return timeSlotFactory.toResponseDTO(timeSlot);
    }

    @Transactional
    public TimeSlotResponseDTO expireTimeSlot(String timeSlotId) {
        log.debug("Expiring time slot: {}", timeSlotId);
        TimeSlot timeSlot = timeSlotRepository.findByTimeSlotId(timeSlotId)
                .orElseThrow(() -> new TimeSlotNotFoundException("TimeSlot not found: " + timeSlotId));
        timeSlot.setStatus(TimeSlotStatus.EXPIRED);
        timeSlot = timeSlotRepository.save(timeSlot);
        return timeSlotFactory.toResponseDTO(timeSlot);
    }

    @Transactional
    public void deleteTimeSlot(UUID id) {
        log.debug("Deleting time slot with id: {}", id);
        timeSlotRepository.deleteById(id);
    }
}
