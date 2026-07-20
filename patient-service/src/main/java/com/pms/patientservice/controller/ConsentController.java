package com.pms.patientservice.controller;

import com.pms.patientservice.dto.request.ConsentRequestDTO;
import com.pms.patientservice.dto.request.ConfirmConsentRequestDTO;
import com.pms.patientservice.service.facade.ConsentFacade;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/consent")
public class ConsentController {

    private final ConsentFacade consentFacade;

    public ConsentController(ConsentFacade consentFacade) {
        this.consentFacade = consentFacade;
    }

    @PostMapping("/request")
    public ResponseEntity<Map<String, Object>> requestConsent(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid ConsentRequestDTO request) {
        String jwtPatientId = jwt.getClaimAsString("patientId");
        if (jwtPatientId == null || !jwtPatientId.equals(request.patientId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", "You can only request consent for yourself"));
        }
        Map<String, Object> result = consentFacade.requestConsent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<Map<String, Object>> confirmConsent(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id,
            @RequestBody @Valid ConfirmConsentRequestDTO request) {
        if (!consentFacade.isOwnConsent(id, jwt.getClaimAsString("patientId"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", "You can only confirm your own consent"));
        }
        Map<String, Object> result = consentFacade.confirmConsent(id, request.code());
        if (!(boolean) result.get("verified")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/revoke")
    public ResponseEntity<Map<String, Object>> revokeConsent(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id) {
        if (!consentFacade.isOwnConsent(id, jwt.getClaimAsString("patientId"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", "You can only revoke your own consent"));
        }
        Map<String, Object> result = consentFacade.revokeConsent(id);
        return ResponseEntity.ok(result);
    }
}
