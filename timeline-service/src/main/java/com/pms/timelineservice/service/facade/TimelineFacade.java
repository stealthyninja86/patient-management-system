package com.pms.timelineservice.service.facade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.timelineservice.dto.response.TimelineResponse;
import com.pms.timelineservice.model.PatientTimeline;
import com.pms.timelineservice.model.TimelineEntry;
import com.pms.timelineservice.repository.PatientTimelineRepository;
import com.pms.timelineservice.service.ConsentGate;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class TimelineFacade {

    private final static Logger log = LoggerFactory.getLogger(TimelineFacade.class);
    private final static long CACHE_TTL_SECONDS = 7200;

    private final ConsentGate consentGate;
    private final PatientTimelineRepository patientTimelineRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    public TimelineFacade(
            ConsentGate consentGate,
            PatientTimelineRepository patientTimelineRepository,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            MeterRegistry meterRegistry
    ){
        this.consentGate = consentGate;
        this.patientTimelineRepository = patientTimelineRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
    }

    private Jwt getJwt() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken();
        }
        return null;
    }

    public TimelineResponse roleStrategy(String role, String patientId) {
        Jwt jwt = getJwt();
        if (jwt == null) {
            return TimelineResponse.error("Authentication required");
        }
        switch (role) {
            case "PATIENT" -> {
                String jwtPatientId = jwt.getClaimAsString("patientId");
                if (jwtPatientId == null || !jwtPatientId.equals(patientId)) {
                    return TimelineResponse.error("You can only view your own timeline");
                }
                return getTimeline(patientId, jwtPatientId, role)
                        .orElse(TimelineResponse.error("Timeline not found"));
            }
            case "DOCTOR" -> {
                String doctorId = jwt.getClaimAsString("doctorId");
                if (doctorId == null || doctorId.isBlank()) {
                    return TimelineResponse.error("Token missing doctorId claim");
                }
                return getTimeline(patientId, doctorId, role)
                        .orElse(TimelineResponse.error("Consent not granted or expired"));
            }
            case "ADMIN" -> {
                return getTimeline(patientId, null, role)
                        .orElse(TimelineResponse.error("Timeline not found"));
            }
            default -> {
                return TimelineResponse.error("Unauthorized role: " + role);
            }
        }
    }

    public Optional<TimelineResponse> getTimeline(String patientId, String requesterId, String role) {
        switch (role) {
            case "DOCTOR" -> {
                Jwt jwt = getJwt();
                if (jwt == null) return Optional.empty();
                String hospitalId = jwt.getClaimAsString("hospitalId");
                if (hospitalId == null || hospitalId.isBlank() || !consentGate.hasConsent(patientId, hospitalId)) {
                    return Optional.empty();
                }
            }
            case "PATIENT", "ADMIN" -> log.info("Patient and admin do not need consent");
            default -> {
                log.info("Invalid role");
                return Optional.empty();
            }
        }

        String cacheKey = "timeline:" + patientId;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            meterRegistry.counter("timeline.cache.hit").increment();
            return Optional.of(deserialize(patientId, cached));
        }
        meterRegistry.counter("timeline.cache.miss").increment();

        var timeline = patientTimelineRepository.findByPatientId(patientId).orElse(null);
        if (timeline == null) return Optional.empty();

        var allEntries = timeline.getEntries();
        var now = Instant.now();

        List<TimelineEntry> upcoming = allEntries.stream()
                .filter(e -> e.getAppointmentStatus() != null &&
                        (e.getAppointmentStatus().equals("PENDING_OTP") || e.getAppointmentStatus().equals("BOOKED")))
                .sorted(Comparator.comparing(TimelineEntry::getStartedAt))
                .toList();

        List<TimelineEntry> history = allEntries.stream()
                .filter(e -> e.getAppointmentStatus() != null &&
                        !e.getAppointmentStatus().equals("PENDING_OTP") && !e.getAppointmentStatus().equals("BOOKED"))
                .sorted(Comparator.comparing(TimelineEntry::getStartedAt).reversed())
                .toList();

        var response = TimelineResponse.success(patientId, upcoming, history, now);
        cacheTimeline(cacheKey, response);
        return Optional.of(response);
    }

    private TimelineResponse deserialize(String patientId, String json) {
        try {
            return objectMapper.readValue(json, TimelineResponse.class);
        } catch (IOException e) {
            log.warn("failed to deserialize timeline for patient {}, evicting", patientId);
            redisTemplate.delete("timeline:" + patientId);
            return null;
        }
    }

    private void cacheTimeline(String cacheKey, TimelineResponse timelineResponse) {
        try {
            redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(timelineResponse),
                    Duration.ofSeconds(CACHE_TTL_SECONDS));
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize timeline for patient {}", timelineResponse.patientId());
        }
    }
}
