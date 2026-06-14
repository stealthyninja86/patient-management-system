package com.pms.patient_service.service.factory;

import com.pms.patient_service.dto.request.PatientGrpcRequestDTO;
import com.pms.patient_service.dto.request.PatientRequestDTO;
import com.pms.patient_service.model.Patient;
import com.pms.patient_service.service.util.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class PatientFactory {

    private static final Logger log = LoggerFactory.getLogger(PatientFactory.class);
    private final IdGenerator idGenerator;

    public PatientFactory(IdGenerator idGenerator) {
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
}
