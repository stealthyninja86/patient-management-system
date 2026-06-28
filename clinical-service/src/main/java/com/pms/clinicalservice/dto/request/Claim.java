package com.pms.clinicalservice.dto.request;

import com.pms.clinicalservice.model.Confidence;

public record Claim(
        String text,
        Confidence status
) {
}
