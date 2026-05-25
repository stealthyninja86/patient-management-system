package com.pms.clinicalservice.dto;

import com.pms.clinicalservice.model.PrescriptionStatus;
import java.time.LocalDateTime;
import java.util.List;

public record PrescriptionResponseDTO(String prescriptionId, List<DrugResponseDTO> drugs, String patientId,
                                      String patientName, String hospitalName, String doctorName,
                                      String departmentName, LocalDateTime consultationDate,
                                      String diagnosis, int painScore, String allergies,
                                      String doctorId, String departmentId, String hospitalId,
                                      PrescriptionStatus status) {
}
