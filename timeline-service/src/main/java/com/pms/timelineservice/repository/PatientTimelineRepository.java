package com.pms.timelineservice.repository;


import com.pms.timelineservice.model.PatientTimeline;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PatientTimelineRepository extends MongoRepository<PatientTimeline, String> {
    Optional<PatientTimeline> findByPatientId(String patientId);
}
