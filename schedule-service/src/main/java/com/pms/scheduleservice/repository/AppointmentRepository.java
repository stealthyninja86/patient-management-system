package com.pms.scheduleservice.repository;

import com.pms.scheduleservice.model.Appointment;
import com.pms.scheduleservice.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    Optional<Appointment> findByAppointmentId(String appointmentId);

    List<Appointment> findByPatientId(String patientId);

    List<Appointment> findByDoctorId(String doctorId);

    List<Appointment> findByTimeSlotId(String timeSlotId);

    Optional<Appointment> findByTimeSlotIdAndStatusIn(String timeSlotId, List<AppointmentStatus> statuses);

    boolean existsByDoctorIdAndPatientIdAndStatus(String doctorId, String patientId, AppointmentStatus appointmentStatus);

    Optional<Appointment> findFirstByDoctorIdAndPatientIdAndStatus(String doctorId, String patientId, AppointmentStatus appointmentStatus);
}
