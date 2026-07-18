package com.pms.notificationservice.service.resolver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NameResolver {

    private static final Logger log = LoggerFactory.getLogger(NameResolver.class);

    private final RestClient patientClient;
    private final RestClient doctorClient;
    private final RestClient hospitalClient;
    private final ObjectMapper objectMapper;

    public NameResolver(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.patientClient = RestClient.builder()
                .baseUrl("http://patient-service:4000")
                .build();
        this.doctorClient = RestClient.builder()
                .baseUrl("http://hospital-service:4003/doctors")
                .build();
        this.hospitalClient = RestClient.builder()
                .baseUrl("http://hospital-service:4003/hospitals")
                .build();
    }

    public String resolvePatientName(String patientId) {
        if (patientId == null || patientId.isBlank()) return null;
        try {
            String json = patientClient.get()
                    .uri("/patients/{id}", patientId)
                    .header("Authorization", "Bearer " + getInternalToken())
                    .retrieve()
                    .body(String.class);
            JsonNode root = objectMapper.readTree(json);
            String name = root.get("name").asText(null);
            log.debug("Resolved patient name: {} for patientId: {}", name, patientId);
            return name;
        } catch (Exception e) {
            log.warn("Failed to resolve patient name for patientId: {}: {}", patientId, e.getMessage());
            return null;
        }
    }

    public String resolveDoctorName(String doctorId) {
        if (doctorId == null || doctorId.isBlank()) return null;
        try {
            String json = doctorClient.get()
                    .uri("/by-doctor-id?doctorId={id}", doctorId)
                    .header("Authorization", "Bearer " + getInternalToken())
                    .retrieve()
                    .body(String.class);
            JsonNode root = objectMapper.readTree(json);
            String name = root.get("name").asText(null);
            log.debug("Resolved doctor name: {} for doctorId: {}", name, doctorId);
            return name;
        } catch (Exception e) {
            log.warn("Failed to resolve doctor name for doctorId: {}: {}", doctorId, e.getMessage());
            return null;
        }
    }

    public String resolveHospitalName(String hospitalId) {
        if (hospitalId == null || hospitalId.isBlank()) return null;
        try {
            String json = hospitalClient.get()
                    .uri("/{id}", hospitalId)
                    .header("Authorization", "Bearer " + getInternalToken())
                    .retrieve()
                    .body(String.class);
            JsonNode root = objectMapper.readTree(json);
            String name = root.get("name").asText(null);
            log.debug("Resolved hospital name: {} for hospitalId: {}", name, hospitalId);
            return name;
        } catch (Exception e) {
            log.warn("Failed to resolve hospital name for hospitalId: {}: {}", hospitalId, e.getMessage());
            return null;
        }
    }

    public String getInternalToken() {
        try {
            RestClient authClient = RestClient.builder()
                    .baseUrl("http://auth-service:4005")
                    .build();
            String authHeader = java.util.Base64.getEncoder()
                    .encodeToString("internal-service:service-secret".getBytes());
            String json = authClient.post()
                    .uri("/oauth2/token")
                    .header("Authorization", "Basic " + authHeader)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body("grant_type=client_credentials")
                    .retrieve()
                    .body(String.class);
            JsonNode root = objectMapper.readTree(json);
            return root.get("access_token").asText();
        } catch (Exception e) {
            log.warn("Failed to obtain internal token for name resolution: {}", e.getMessage());
            return "";
        }
    }
}
