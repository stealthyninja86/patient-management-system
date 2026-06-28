package com.pms.clinicalservice.dto.event;

import com.pms.clinicalservice.dto.request.Claim;

import java.util.List;

public record UnverifiedAlertEvent (
        String prescriptionId,
        String summary,
        List<Claim> claims,
        String webContext
){
}
