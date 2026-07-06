package com.pms.timelineservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "patient_timelines")
public class PatientTimeline {
    @Id
    private String id;

    @Indexed(unique = true)
    private String patientId;
    private List<TimelineEntry> entries;
    private Instant cachedAt;

    public List<TimelineEntry> getEntries() { return entries; }
}



