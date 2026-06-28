package com.pms.clinicalservice.dto.response;

import com.pms.clinicalservice.model.PrescriptionStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;

public record PrescriptionResponseDTO(String prescriptionId, List<DrugResponseDTO> drugs, String patientId,
                                      String patientName, String hospitalName, String doctorName,
                                      String departmentName,
                                      @JsonFormat(pattern = "dd-MMM-yyyy HH:mm") LocalDateTime consultationDate,
                                      String diagnosis, int painScore, String allergies,
                                      String doctorId, String departmentId, String hospitalId,
                                      PrescriptionStatus status,
                                      String patientPhone, String patientEmail, String doctorPhone,
                                      String doctorEmail, String hospitalPhone, String hospitalEmail,
                                      String hospitalWebsite, String doctorNote) {
}
