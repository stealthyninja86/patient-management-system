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
                                String hospitalName, String doctorName,
                                String appointmentStatus, String startTime,
                                String endTime
    ){
        Query query = Query.query(
                Criteria.where("patientId").is(patientId)
        );
        Update update = new Update()
                .set("entries.$[elem].appointmentId", appointmentId)
                .set("entries.$[elem].appointmentStatus", appointmentStatus)
                .set("entries.$[elem].doctorName", doctorName)
                .set("entries.$[elem].hospitalName", hospitalName)
                .set("entries.$[elem].startedAt", startTime)
                .set("entries.$[elem].endedAt", endTime)
                .set("cachedAt", Instant.now())
                .filterArray(Criteria.where("elem.appointmentId").is(appointmentId));

        var result = mongoTemplate.updateMulti(query, update, PatientTimeline.class);

        if(result.getModifiedCount() == 0 && result.getMatchedCount() == 0){
            TimelineEntry entry = new  TimelineEntry();
            entry.setId(UUID.randomUUID().toString());
            entry.setAppointmentId(appointmentId);
            entry.setHospitalName(hospitalName);
            entry.setDoctorName(doctorName);
            entry.setAppointmentStatus(appointmentStatus);
            entry.setStartedAt(Instant.parse(startTime));
            entry.setEndedAt(Instant.parse(endTime));
            entry.setPrescriptions(new ArrayList<>());

            Query pushQuery = Query.query(
                    Criteria.where("patientId").is(patientId)
                            .and("entries.appointmentId").ne(appointmentId)
            );
            Update pushUpdate = new  Update()
                    .push("entries", entry)
                    .set("cachedAt", Instant.now()
                    );
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
        );

        Update update = new Update()
                .push("entries.$[elem].prescriptions",
                        Map.of("prescriptionId", prescriptionId,
                        "status", status))
                .filterArray(Criteria.where("elem.appointmentId").is(appointmentId));

        mongoTemplate.updateFirst(query, update, PatientTimeline.class);

        meterRegistry.counter("timeline.events.consumed",
                "eventType",
                "PRESCRIPTION").increment();
    }
}
