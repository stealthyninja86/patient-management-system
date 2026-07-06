package com.pms.timelineservice.controller;

import com.pms.timelineservice.dto.response.TimelineResponse;
import com.pms.timelineservice.service.facade.TimelineFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TimelineController {
    private final TimelineFacade timelineFacade;

    public TimelineController(TimelineFacade timelineFacade) {
        this.timelineFacade = timelineFacade;
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<TimelineResponse> getPatientTimeline(
            @PathVariable String patientId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String role = jwt.getClaimAsString("role");
        TimelineResponse response = timelineFacade.roleStrategy(role, patientId);
        if (response.error() != null) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

}
