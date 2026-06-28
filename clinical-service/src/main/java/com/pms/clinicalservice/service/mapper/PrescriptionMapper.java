package com.pms.clinicalservice.service.mapper;

import com.pms.clinicalservice.dto.response.DrugResponseDTO;
import com.pms.clinicalservice.dto.request.PrescriptionRequestDTO;
import com.pms.clinicalservice.dto.response.PrescriptionResponseDTO;
import com.pms.clinicalservice.model.Drug;
import com.pms.clinicalservice.model.Prescription;
import com.pms.clinicalservice.model.PrescriptionStatus;
import hospital.DepartmentResponse;
import hospital.DoctorResponse;
import hospital.HospitalResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import patient.PatientResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class PrescriptionMapper {

    private static final Logger log = LoggerFactory.getLogger(PrescriptionMapper.class);

    public PrescriptionResponseDTO toPrescriptionResponseDTO(Prescription prescription) {
        log.debug("Converting Prescription entity to PrescriptionResponseDTO: {}", prescription.getPrescriptionId());
        List<DrugResponseDTO> drugDTOs = prescription.getDrugs().stream()
                .map(this::toDrugResponseDTO)
                .collect(Collectors.toList());
        return new PrescriptionResponseDTO(
                prescription.getPrescriptionId(),
                drugDTOs,
                prescription.getPatientId(),
                prescription.getPatientName(),
                prescription.getHospitalName(),
                prescription.getDoctorName(),
                prescription.getDepartmentName(),
                prescription.getConsultationDate(),
                prescription.getDiagnosis(),
                prescription.getPainScore(),
                prescription.getAllergies(),
                prescription.getDoctorId(),
                prescription.getDepartmentId(),
                prescription.getHospitalId(),
                prescription.getStatus(),
                prescription.getPatientPhone(),
                prescription.getPatientEmail(),
                prescription.getDoctorPhone(),
                prescription.getDoctorEmail(),
                prescription.getHospitalPhone(),
                prescription.getHospitalEmail(),
                prescription.getHospitalWebsite(),
                prescription.getDoctorNote()
        );
    }

    public DrugResponseDTO toDrugResponseDTO(Drug drug) {
        log.debug("Converting Drug entity to DrugResponseDTO: {}", drug.getDrugId());
        return new DrugResponseDTO(
                drug.getDrugId(),
                drug.getName(),
                drug.getDosage(),
                drug.getDescription(),
                drug.getUsage(),
                drug.getType()
        );
    }

    public Prescription toPrescriptionEntity(PrescriptionRequestDTO request, String doctorId,
                                              DoctorResponse doctorResponse,
                                              DepartmentResponse deptResponse,
                                              HospitalResponse hospitalResponse,
                                              PatientResponse patientResponse,
                                              List<Drug> drugs,
                                              LocalDateTime consultationDate) {
        log.debug("Converting PrescriptionRequestDTO to Prescription entity for doctor: {}, patient: {}", doctorId, request.patientId());
        return Prescription.builder()
                .prescriptionId(null)
                .idempotencyKey(request.idempotencyKey())
                .doctorId(doctorId)
                .doctorName(doctorResponse.getName())
                .doctorEmail(doctorResponse.getEmail())
                .doctorPhone(doctorResponse.getPhone())
                .departmentId(deptResponse.getDepartmentId())
                .departmentName(deptResponse.getName())
                .hospitalId(deptResponse.getHospitalId())
                .hospitalName(deptResponse.getHospitalName())
                .hospitalPhone(hospitalResponse.getPhone())
                .hospitalEmail(hospitalResponse.getEmail())
                .hospitalWebsite(hospitalResponse.getWebsite())
                .hospitalAddress(hospitalResponse.getAddress())
                .patientId(patientResponse.getPatientId())
                .patientName(patientResponse.getName())
                .patientPhone(patientResponse.getPhone())
                .patientEmail(patientResponse.getEmail())
                .patientGender(patientResponse.getGender())
                .patientDateOfBirth(LocalDate.parse(patientResponse.getDateOfBirth(), DateTimeFormatter.ISO_LOCAL_DATE))
                .diagnosis(request.diagnosis())
                .painScore(request.painScore())
                .allergies(request.allergies())
                .followUpWeeks(request.followUpWeeks())
                .consultationDate(consultationDate)
                .doctorNote(request.doctorNote())
                .drugs(drugs)
                .status(PrescriptionStatus.ACTIVE)
                .build();
    }

    public Drug toDrugEntity(PrescriptionRequestDTO.DrugInput drugInput, String drugId) {
        log.debug("Converting DrugInput to Drug entity with drugId: {}", drugId);
        return Drug.builder()
                .drugId(drugId)
                .name(drugInput.name())
                .dosage(drugInput.dosage())
                .description(drugInput.description())
                .usage(drugInput.usage())
                .type(drugInput.type())
                .build();
    }
}
