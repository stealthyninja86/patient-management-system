package com.pms.clinicalservice.factory;

import com.pms.clinicalservice.dto.DrugResponseDTO;
import com.pms.clinicalservice.dto.PrescriptionRequestDTO;
import com.pms.clinicalservice.dto.PrescriptionResponseDTO;
import com.pms.clinicalservice.model.Drug;
import com.pms.clinicalservice.model.Prescription;
import com.pms.clinicalservice.model.PrescriptionStatus;
import hospital.DepartmentResponse;
import hospital.DoctorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import patient.PatientResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class PrescriptionFactory {

    private static final Logger log = LoggerFactory.getLogger(PrescriptionFactory.class);

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
                prescription.getStatus()
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
                                              PatientResponse patientResponse,
                                              List<Drug> drugs) {
        log.debug("Converting PrescriptionRequestDTO to Prescription entity for doctor: {}, patient: {}", doctorId, request.patientId());
        return Prescription.builder()
                .prescriptionId(null)
                .doctorId(doctorId)
                .doctorName(doctorResponse.getName())
                .departmentId(deptResponse.getDepartmentId())
                .departmentName(deptResponse.getName())
                .hospitalId(deptResponse.getHospitalId())
                .hospitalName(deptResponse.getHospitalName())
                .patientId(patientResponse.getPatientId())
                .patientName(patientResponse.getName())
                .diagnosis(request.diagnosis())
                .painScore(request.painScore())
                .allergies(request.allergies())
                .consultationDate(LocalDateTime.now())
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
