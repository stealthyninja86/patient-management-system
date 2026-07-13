package com.pms.timelineservice.service;

import com.pms.timelineservice.model.PatientTimeline;
import com.pms.timelineservice.model.TimelineEntry;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TimelineIngestionService {
    private static final Logger LOG = LoggerFactory.getLogger(TimelineIngestionService.class);

    private final MongoTemplate mongoTemplate;
    private final MeterRegistry meterRegistry;

    public TimelineIngestionService(MongoTemplate mongoTemplate, MeterRegistry meterRegistry) {
        this.mongoTemplate = mongoTemplate;
        this.meterRegistry = meterRegistry;
    }

    public void upsertEncounter(String patientId, String appointmentId,
                                String doctorId, String doctorName,
                                String hospitalId, String hospitalName,
                                String appointmentStatus, String startTime,
                                String endTime
    ){
        Instant startedAt;
        try { startedAt = startTime != null ? Instant.parse(startTime) : Instant.now(); }
        catch (Exception e) { startedAt = Instant.now(); }

        Instant endedAt;
        try { endedAt = endTime != null ? Instant.parse(endTime) : null; }
        catch (Exception e) { endedAt = null; }

        Query query = Query.query(
                Criteria.where("patientId").is(patientId)
                        .and("entries.appointmentId").is(appointmentId)
        );
        Update update = new Update()
                .set("entries.$.appointmentStatus", appointmentStatus)
                .set("entries.$.doctorId", doctorId)
                .set("entries.$.doctorName", doctorName)
                .set("entries.$.hospitalId", hospitalId)
                .set("entries.$.hospitalName", hospitalName)
                .set("entries.$.startedAt", startedAt)
                .set("entries.$.endedAt", endedAt)
                .set("cachedAt", Instant.now());

        var result = mongoTemplate.updateFirst(query, update, PatientTimeline.class);

        if(result.getModifiedCount() == 0 && result.getMatchedCount() == 0){
            TimelineEntry entry = new  TimelineEntry();
            entry.setId(UUID.randomUUID().toString());
            entry.setAppointmentId(appointmentId);
            entry.setDoctorId(doctorId);
            entry.setDoctorName(doctorName);
            entry.setHospitalId(hospitalId);
            entry.setHospitalName(hospitalName);
            entry.setAppointmentStatus(appointmentStatus);
            entry.setStartedAt(startedAt);
            entry.setEndedAt(endedAt);
            entry.setPrescriptions(new ArrayList<>());

            Query pushQuery = Query.query(
                    Criteria.where("patientId").is(patientId)
                            .and("entries.appointmentId").ne(appointmentId)
            );
            Update pushUpdate = new  Update()
                    .push("entries", entry)
                    .set("cachedAt", Instant.now());
            mongoTemplate.upsert(pushQuery, pushUpdate, PatientTimeline.class);
        }

        meterRegistry.counter("timeline.events.consumed",
                "eventType",
                "APPOINTMENT").increment();
    }

    public void addPrescriptionToEncounter(String patientId, String appointmentId,
                                           String prescriptionId, String status){
        Query query = Query.query(
                Criteria.where("patientId").is(patientId)
                        .and("entries.appointmentId").is(appointmentId)
        );

        Update update = new Update()
                .push("entries.$.prescriptions",
                        Map.of("prescriptionId", prescriptionId,
                        "status", status));

        mongoTemplate.updateFirst(query, update, PatientTimeline.class);

        meterRegistry.counter("timeline.events.consumed",
                "eventType",
                "PRESCRIPTION").increment();
    }
}
