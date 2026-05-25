package com.pms.scheduleservice.facade;

import com.pms.scheduleservice.dto.TimeSlotRequestDTO;
import com.pms.scheduleservice.dto.TimeSlotResponseDTO;
import com.pms.scheduleservice.model.TimeSlotStatus;
import com.pms.scheduleservice.repository.TimeSlotRepository;
import com.pms.scheduleservice.service.TimeSlotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class TimeSlotFacade {

    private static final Logger log = LoggerFactory.getLogger(TimeSlotFacade.class);

    private final TimeSlotService timeSlotService;
    private final TimeSlotRepository timeSlotRepository;

    public TimeSlotFacade(TimeSlotService timeSlotService,
                           TimeSlotRepository timeSlotRepository) {
        this.timeSlotService = timeSlotService;
        this.timeSlotRepository = timeSlotRepository;
    }

    public List<TimeSlotResponseDTO> getAllTimeSlots() {
        log.debug("Fetching all time slots via facade");
        return timeSlotService.getAllTimeSlots();
    }

    public TimeSlotResponseDTO getTimeSlotById(String timeSlotId) {
        log.debug("Fetching time slot by id via facade: {}", timeSlotId);
        return timeSlotService.getTimeSlotById(timeSlotId);
    }

    public List<TimeSlotResponseDTO> getTimeSlotsByDoctor(String doctorId) {
        log.debug("Fetching time slots by doctor via facade: {}", doctorId);
        return timeSlotService.getTimeSlotsByDoctor(doctorId);
    }

    public List<TimeSlotResponseDTO> getAvailableTimeSlotsByDoctor(String doctorId) {
        log.debug("Fetching available time slots by doctor via facade: {}", doctorId);
        return timeSlotService.getAvailableTimeSlotsByDoctor(doctorId);
    }

    public List<TimeSlotResponseDTO> getTimeSlotsByHospital(String hospitalId) {
        log.debug("Fetching time slots by hospital via facade: {}", hospitalId);
        return timeSlotService.getTimeSlotsByHospital(hospitalId);
    }

    public TimeSlotResponseDTO createTimeSlot(TimeSlotRequestDTO request) {
        log.debug("Creating time slot via facade for doctor: {}", request.doctorId());
        return timeSlotService.createTimeSlot(request);
    }

    public TimeSlotResponseDTO expireTimeSlot(String timeSlotId) {
        log.debug("Expiring time slot via facade: {}", timeSlotId);
        return timeSlotService.expireTimeSlot(timeSlotId);
    }

    public TimeSlotResponseDTO updateTimeSlotStatus(String timeSlotId, TimeSlotStatus status) {
        log.debug("Updating time slot status via facade: {} to {}", timeSlotId, status);
        return timeSlotService.updateTimeSlotStatus(timeSlotId, status);
    }

    public void deleteTimeSlot(UUID id) {
        log.debug("Deleting time slot via facade: {}", id);
        timeSlotService.deleteTimeSlot(id);
    }
}
