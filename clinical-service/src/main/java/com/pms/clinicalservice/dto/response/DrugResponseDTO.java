package com.pms.clinicalservice.dto.response;

import com.pms.clinicalservice.model.DrugType;

public record DrugResponseDTO(String drugId, String name, String dosage, String description, String usage, DrugType type) {
}
