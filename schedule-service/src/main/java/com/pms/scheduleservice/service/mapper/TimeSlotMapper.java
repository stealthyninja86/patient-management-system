package com.pms.scheduleservice.service.mapper;

import com.pms.scheduleservice.dto.request.TimeSlotRequestDTO;
import com.pms.scheduleservice.dto.response.TimeSlotResponseDTO;
import com.pms.scheduleservice.model.TimeSlot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TimeSlotMapper {

    private static final Logger log = LoggerFactory.getLogger(TimeSlotMapper.class);

    public TimeSlot toEntity(TimeSlotRequestDTO request, String timeSlotId, String doctorName) {
        log.debug("Converting TimeSlotRequestDTO to entity for timeSlotId: {}", timeSlotId);
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setTimeSlotId(timeSlotId);
        timeSlot.setDoctorId(request.doctorId());
        timeSlot.setDoctorName(doctorName);
        timeSlot.setStartTime(request.startTime());
        timeSlot.setEndTime(request.endTime());
        return timeSlot;
    }

    public TimeSlotResponseDTO toResponseDTO(TimeSlot timeSlot, String status) {
        log.debug("Converting TimeSlot to ResponseDTO for timeSlotId: {}", timeSlot.getTimeSlotId());
        return new TimeSlotResponseDTO(
            timeSlot.getTimeSlotId(),
            timeSlot.getDoctorId(),
            timeSlot.getDoctorName(),
            timeSlot.getStartTime(),
            timeSlot.getEndTime(),
            status
        );
    }

    public TimeSlotResponseDTO toResponseDTO(TimeSlot timeSlot) {
        return toResponseDTO(timeSlot, "UNKNOWN");
    }
}
