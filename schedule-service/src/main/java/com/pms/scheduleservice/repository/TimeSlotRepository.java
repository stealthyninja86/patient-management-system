package com.pms.scheduleservice.repository;

import com.pms.scheduleservice.model.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, UUID> {

    Optional<TimeSlot> findByTimeSlotId(String timeSlotId);

    List<TimeSlot> findByDoctorId(String doctorId);
}
