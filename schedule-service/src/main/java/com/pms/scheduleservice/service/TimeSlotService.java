package com.pms.scheduleservice.service;

import com.pms.scheduleservice.dto.TimeSlotRequestDTO;
import com.pms.scheduleservice.dto.TimeSlotResponseDTO;
import com.pms.scheduleservice.exception.DoctorNotFoundException;
import com.pms.scheduleservice.exception.TimeSlotNotFoundException;
import com.pms.scheduleservice.service.factory.TimeSlotFactory;
import com.pms.scheduleservice.grpc.DoctorGrpcClient;
import com.pms.scheduleservice.model.AppointmentStatus;
import com.pms.scheduleservice.model.TimeSlot;
import com.pms.scheduleservice.repository.AppointmentRepository;
import com.pms.scheduleservice.repository.TimeSlotRepository;
import com.pms.scheduleservice.service.util.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class TimeSlotService {

    private static final Logger log = LoggerFactory.getLogger(TimeSlotService.class);

    private final TimeSlotRepository timeSlotRepository;
    private final AppointmentRepository appointmentRepository;
    private final IdGenerator idGenerator;
    private final TimeSlotFactory timeSlotFactory;
    private final DoctorGrpcClient doctorGrpcClient;

    public TimeSlotService(TimeSlotRepository timeSlotRepository,
                            AppointmentRepository appointmentRepository,
                            IdGenerator idGenerator,
                            TimeSlotFactory timeSlotFactory,
                            DoctorGrpcClient doctorGrpcClient) {
        this.timeSlotRepository = timeSlotRepository;
        this.appointmentRepository = appointmentRepository;
        this.idGenerator = idGenerator;
        this.timeSlotFactory = timeSlotFactory;
        this.doctorGrpcClient = doctorGrpcClient;
    }

    public List<TimeSlotResponseDTO> getAllTimeSlots() {
        log.debug("Fetching all time slots");
        Set<String> bookedIds = getBookedTimeSlotIds();
        return timeSlotRepository.findAll().stream()
                .map(slot -> timeSlotFactory.toResponseDTO(slot, resolveStatus(slot.getTimeSlotId(), bookedIds)))
                .toList();
    }

    public TimeSlotResponseDTO getTimeSlotById(String timeSlotId) {
        log.debug("Fetching time slot by id: {}", timeSlotId);
        TimeSlot timeSlot = timeSlotRepository.findByTimeSlotId(timeSlotId)
                .orElseThrow(() -> new TimeSlotNotFoundException("TimeSlot not found: " + timeSlotId));
        Set<String> bookedIds = getBookedTimeSlotIds();
        return timeSlotFactory.toResponseDTO(timeSlot, resolveStatus(timeSlotId, bookedIds));
    }

    public List<TimeSlotResponseDTO> getTimeSlotsByDoctor(String doctorId) {
        log.debug("Fetching time slots by doctor: {}", doctorId);
        Set<String> bookedIds = getBookedTimeSlotIds();
        return timeSlotRepository.findByDoctorId(doctorId).stream()
                .map(slot -> timeSlotFactory.toResponseDTO(slot, resolveStatus(slot.getTimeSlotId(), bookedIds)))
                .toList();
    }

    public List<TimeSlotResponseDTO> getAvailableTimeSlotsByDoctor(String doctorId) {
        log.debug("Fetching available time slots by doctor: {}", doctorId);
        Set<String> bookedIds = getBookedTimeSlotIds();
        List<TimeSlot> allSlots = timeSlotRepository.findByDoctorId(doctorId);
        return allSlots.stream()
                .filter(slot -> !bookedIds.contains(slot.getTimeSlotId()))
                .map(slot -> timeSlotFactory.toResponseDTO(slot, "AVAILABLE"))
                .toList();
    }

    private Set<String> getBookedTimeSlotIds() {
        List<String> booked = appointmentRepository.findBookedTimeSlotIds(
                List.of(AppointmentStatus.BOOKED, AppointmentStatus.ONGOING));
        return booked.isEmpty() ? Collections.emptySet() : Set.copyOf(booked);
    }

    private String resolveStatus(String timeSlotId, Set<String> bookedIds) {
        return bookedIds.contains(timeSlotId) ? "BOOKED" : "AVAILABLE";
    }

    @Transactional
    public TimeSlotResponseDTO createTimeSlot(TimeSlotRequestDTO request) {
        log.info("Creating time slot for doctor: {}", request.doctorId());
        String doctorName;
        try {
            doctorName = doctorGrpcClient.getDoctorById(request.doctorId()).getName();
        } catch (Exception e) {
            throw new DoctorNotFoundException("Doctor not found with id: " + request.doctorId());
        }
        if(doctorName == null || !doctorName.equals(request.doctorName())) {
            throw new DoctorNotFoundException("Doctor not found with id: " + request.doctorName());
        }
        String timeSlotId = idGenerator.nextId("TS", "time_slot_seq");
        TimeSlot timeSlot = timeSlotFactory.toEntity(request, timeSlotId, doctorName);
        timeSlot = timeSlotRepository.save(timeSlot);
        return timeSlotFactory.toResponseDTO(timeSlot, "AVAILABLE");
    }

    @Transactional
    public void deleteTimeSlot(UUID id) {
        log.debug("Deleting time slot with id: {}", id);
        timeSlotRepository.deleteById(id);
    }
}
