package com.pms.patientservice.controller;

import com.pms.patientservice.dto.request.ConsentRequestDTO;
import com.pms.patientservice.dto.request.ConfirmConsentRequestDTO;
import com.pms.patientservice.service.facade.ConsentFacade;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            @RequestBody @Valid ConsentRequestDTO request) {
        Map<String, Object> result = consentFacade.requestConsent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<Map<String, Object>> confirmConsent(
            @PathVariable String id,
            @RequestBody @Valid ConfirmConsentRequestDTO request) {
        Map<String, Object> result = consentFacade.confirmConsent(id, request.code());
        if (!(boolean) result.get("verified")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }
        return ResponseEntity.ok(result);
    }
}
