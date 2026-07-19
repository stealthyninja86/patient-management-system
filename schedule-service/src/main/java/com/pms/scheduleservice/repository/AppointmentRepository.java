package com.pms.scheduleservice.repository;

import com.pms.scheduleservice.model.Appointment;
import com.pms.scheduleservice.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    @Query("SELECT DISTINCT a.timeSlotId FROM Appointment a WHERE a.status IN :statuses")
    List<String> findBookedTimeSlotIds(@Param("statuses") List<AppointmentStatus> statuses);

    boolean existsByDoctorIdAndPatientIdAndStatus(String doctorId, String patientId, AppointmentStatus appointmentStatus);

    Optional<Appointment> findFirstByDoctorIdAndPatientIdAndStatus(String doctorId, String patientId, AppointmentStatus appointmentStatus);

    Optional<Appointment> findFirstByDoctorIdAndPatientIdAndStatusIn(String doctorId, String patientId, List<AppointmentStatus> statuses);

    @Modifying
    @Query("UPDATE Appointment a SET a.status = 'CANCELLED' WHERE a.status = :status AND a.createdAt < :cutoff")
    int expireByStatusAndCreatedBefore(@Param("status") AppointmentStatus status, @Param("cutoff") LocalDateTime cutoff);

    @Modifying
    @Query("DELETE FROM Appointment a WHERE a.status = 'CANCELLED' AND a.timeSlotId IN " +
           "(SELECT t.timeSlotId FROM TimeSlot t WHERE t.endTime < :cutoff)")
    int purgeCancelledAppointments(@Param("cutoff") LocalDateTime cutoff);
}
