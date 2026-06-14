package com.pms.scheduleservice.service.facade;

import com.pms.scheduleservice.dto.TimeSlotRequestDTO;
import com.pms.scheduleservice.dto.TimeSlotResponseDTO;
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

    public TimeSlotFacade(TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
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

    public TimeSlotResponseDTO createTimeSlot(TimeSlotRequestDTO request) {
        log.debug("Creating time slot via facade for doctor: {}", request.doctorId());
        return timeSlotService.createTimeSlot(request);
    }

    public void deleteTimeSlot(UUID id) {
        log.debug("Deleting time slot via facade: {}", id);
        timeSlotService.deleteTimeSlot(id);
    }
}
