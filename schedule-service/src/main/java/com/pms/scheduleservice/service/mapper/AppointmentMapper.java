package com.pms.scheduleservice.service.mapper;

import com.pms.scheduleservice.dto.event.AppointmentEventDTO;
import com.pms.scheduleservice.dto.request.AppointmentRequestDTO;
import com.pms.scheduleservice.dto.response.AppointmentResponseDTO;
import com.pms.scheduleservice.model.Appointment;
import com.pms.scheduleservice.model.AppointmentStatus;
import com.pms.scheduleservice.model.TimeSlot;
import com.pms.scheduleservice.repository.TimeSlotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class AppointmentMapper {

    private static final Logger log = LoggerFactory.getLogger(AppointmentMapper.class);
    private final TimeSlotRepository timeSlotRepository;

    public AppointmentMapper(TimeSlotRepository timeSlotRepository) {
        this.timeSlotRepository = timeSlotRepository;
    }

    public Appointment toEntity(AppointmentRequestDTO request, String appointmentId, TimeSlot timeSlot) {
        log.debug("Converting AppointmentRequestDTO to entity for appointmentId: {}", appointmentId);
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(appointmentId);
        appointment.setPatientId(request.patientId());
        appointment.setDoctorId(timeSlot.getDoctorId());
        appointment.setDoctorName(timeSlot.getDoctorName());
        appointment.setHospitalId(timeSlot.getHospitalId());
        appointment.setHospitalName(timeSlot.getHospitalName());
        appointment.setTimeSlotId(request.timeSlotId());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("hh:mm a");
        appointment.setTimeSlotName(
            timeSlot.getStartTime().format(fmt) + " - " + timeSlot.getEndTime().format(fmt));
        return appointment;
    }

    public AppointmentResponseDTO toResponseDTO(Appointment appointment) {
        log.debug("Converting Appointment to ResponseDTO for appointmentId: {}", appointment.getAppointmentId());
        return new AppointmentResponseDTO(
            appointment.getAppointmentId(),
            appointment.getPatientId(),
            appointment.getPatientName(),
            appointment.getPatientEmail(),
            appointment.getDoctorId(),
            appointment.getDoctorName(),
            appointment.getTimeSlotId(),
            appointment.getTimeSlotName(),
            appointment.getStatus(),
            appointment.getCreatedAt()
        );
    }

    public AppointmentEventDTO toEventDTO(Appointment appointment, String eventType,
                                          String patientPhone, String hospitalId,
                                          String hospitalName, String appointmentDate) {
        log.debug("Converting Appointment to EventDTO for appointmentId: {}, eventType: {}", appointment.getAppointmentId(), eventType);
        String startTime = null;
        String endTime = null;
        String resolvedDate = appointmentDate;
        if (appointment.getTimeSlotId() != null) {
            var tsOpt = timeSlotRepository.findByTimeSlotId(appointment.getTimeSlotId());
            if (tsOpt.isPresent()) {
                var ts = tsOpt.get();
                if (ts.getStartTime() != null) {
                    startTime = ts.getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";
                    if (resolvedDate == null) {
                        resolvedDate = ts.getStartTime().toLocalDate().toString();
                    }
                }
                if (ts.getEndTime() != null) {
                    endTime = ts.getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";
                }
            }
        }
        return new AppointmentEventDTO(
            eventType,
            appointment.getAppointmentId(),
            appointment.getPatientId(),
            appointment.getPatientName(),
            appointment.getPatientEmail(),
            patientPhone,
            appointment.getDoctorId(),
            appointment.getDoctorName(),
            hospitalId,
            hospitalName,
            appointment.getTimeSlotId(),
            appointment.getStatus().name(),
            resolvedDate,
            startTime,
            endTime
        );
    }
}
