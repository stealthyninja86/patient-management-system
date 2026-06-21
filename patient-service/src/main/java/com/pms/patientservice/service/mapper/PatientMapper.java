package com.pms.patientservice.service.mapper;

import com.pms.patientservice.dto.request.PatientGrpcRequestDTO;
import com.pms.patientservice.dto.request.PatientRequestDTO;
import com.pms.patientservice.dto.response.PatientResponseDTO;
import com.pms.patientservice.model.Patient;
import com.pms.patientservice.service.util.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class PatientMapper {

    private static final Logger log = LoggerFactory.getLogger(PatientMapper.class);
    private final IdGenerator idGenerator;

    public PatientMapper(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public Patient createPatient(PatientRequestDTO dto) {
        log.debug("Creating Patient entity from PatientRequestDTO for email: {}", dto.getEmail());
        return Patient.builder()
                .patientId(idGenerator.nextId("PMS-", "patient_id_seq"))
                .name(dto.getName())
                .email(dto.getEmail())
                .address(dto.getAddress())
                .dateOfBirth(LocalDate.parse(dto.getDateOfBirth()))
                .registeredDate(dto.getRegisteredDate() != null ? LocalDate.parse(dto.getRegisteredDate()) : LocalDate.now())
                .phone(dto.getPhone())
                .gender(dto.getGender())
                .bloodType(dto.getBloodType())
                .build();
    }

    public Patient createPatient(PatientGrpcRequestDTO dto) {
        log.debug("Creating Patient entity from PatientGrpcRequestDTO for email: {}", dto.email());
        return Patient.builder()
                .patientId(idGenerator.nextId("PMS-", "patient_id_seq"))
                .name(dto.name())
                .email(dto.email())
                .address(dto.address() != null && !dto.address().isBlank() ? dto.address() : "N/A")
                .dateOfBirth(dto.dateOfBirth() != null && !dto.dateOfBirth().isBlank()
                        ? LocalDate.parse(dto.dateOfBirth()) : LocalDate.of(2000, 1, 1))
                .registeredDate(dto.registeredDate() != null && !dto.registeredDate().isBlank()
                        ? LocalDate.parse(dto.registeredDate()) : LocalDate.now())
                .phone(dto.phone())
                .gender(dto.gender())
                .bloodType(dto.bloodType())
                .build();
    }

    public void updatePatient(Patient existing, PatientRequestDTO dto) {
        log.debug("Updating Patient entity for patientId: {}", existing.getPatientId());
        if (dto.getName() != null) existing.setName(dto.getName());
        if (dto.getEmail() != null) existing.setEmail(dto.getEmail());
        if (dto.getAddress() != null) existing.setAddress(dto.getAddress());
        if (dto.getDateOfBirth() != null) existing.setDateOfBirth(LocalDate.parse(dto.getDateOfBirth()));
        if (dto.getPhone() != null) existing.setPhone(dto.getPhone());
        if (dto.getGender() != null) existing.setGender(dto.getGender());
        if (dto.getBloodType() != null) existing.setBloodType(dto.getBloodType());
    }

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
