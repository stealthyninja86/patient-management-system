package com.pms.scheduleservice.factory;

import com.pms.scheduleservice.dto.TimeSlotRequestDTO;
import com.pms.scheduleservice.dto.TimeSlotResponseDTO;
import com.pms.scheduleservice.model.TimeSlot;
import com.pms.scheduleservice.model.TimeSlotStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeSlotFactory {

    private static final Logger log = LoggerFactory.getLogger(TimeSlotFactory.class);

    public TimeSlot toEntity(TimeSlotRequestDTO request, String timeSlotId) {
        log.debug("Converting TimeSlotRequestDTO to entity for timeSlotId: {}", timeSlotId);
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setTimeSlotId(timeSlotId);
        timeSlot.setDoctorId(request.doctorId());
        timeSlot.setDoctorName(request.doctorName());
        timeSlot.setHospitalId(request.hospitalId());
        timeSlot.setStartTime(request.startTime());
        timeSlot.setEndTime(request.endTime());
        timeSlot.setStatus(TimeSlotStatus.AVAILABLE);
        return timeSlot;
    }

    public TimeSlotResponseDTO toResponseDTO(TimeSlot timeSlot) {
        log.debug("Converting TimeSlot to ResponseDTO for timeSlotId: {}", timeSlot.getTimeSlotId());
        return new TimeSlotResponseDTO(
            timeSlot.getTimeSlotId(),
            timeSlot.getDoctorId(),
            timeSlot.getDoctorName(),
            timeSlot.getHospitalId(),
            timeSlot.getStartTime(),
            timeSlot.getEndTime(),
            timeSlot.getStatus()
        );
    }
}
