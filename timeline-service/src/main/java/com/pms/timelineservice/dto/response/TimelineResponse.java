package com.pms.timelineservice.dto.response;

import com.pms.timelineservice.model.TimelineEntry;

import java.time.Instant;
import java.util.List;

public record TimelineResponse(
        String patientId,
        List<TimelineEntry> upcoming,
        List<TimelineEntry> history,
        Instant cachedAt,
        String error
) {
    public static TimelineResponse success(String patientId, List<TimelineEntry> upcoming,
                                           List<TimelineEntry> history, Instant cachedAt) {
        return new TimelineResponse(patientId, upcoming, history, cachedAt, null);
    }

    public static TimelineResponse error(String message) {
        return new TimelineResponse(null, null, null, null, message);
    }
}
