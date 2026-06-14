package com.pms.patient_service.service.mapper;

import com.pms.patient_service.dto.response.PatientResponseDTO;
import com.pms.patient_service.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {

    private static final Logger log = LoggerFactory.getLogger(PatientMapper.class);

    public PatientResponseDTO toResponseDTO(Patient patient) {
        log.debug("Converting Patient entity to PatientResponseDTO for patientId: {}", patient.getPatientId());
        return new PatientResponseDTO(
                patient.getId().toString(),
                patient.getPatientId(),
                patient.getName(),
                patient.getEmail(),
                patient.getAddress(),
                patient.getDateOfBirth().toString(),
                patient.getRegisteredDate() != null ? patient.getRegisteredDate().toString() : null,
                patient.getPhone(),
                patient.getGender(),
                patient.getBloodType()
        );
    }
}
