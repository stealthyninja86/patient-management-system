package com.pms.authservice.dto.response;

public record LoginResponseDTO(String token, String role, String doctorId, String patientId) {

    public static LoginResponseDTO forPatient(String token, String patientId) {
        return new LoginResponseDTO(token, "PATIENT", null, patientId);
    }

    public static LoginResponseDTO forDoctor(String token, String doctorId) {
        return new LoginResponseDTO(token, "DOCTOR", doctorId, null);
    }
}
