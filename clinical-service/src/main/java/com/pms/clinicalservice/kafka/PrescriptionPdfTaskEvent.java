package com.pms.clinicalservice.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PrescriptionPdfTaskEvent {

    @JsonProperty("prescriptionId")
    private String prescriptionId;

    @JsonProperty("eventType")
    private String eventType;

    public PrescriptionPdfTaskEvent() {}

    public PrescriptionPdfTaskEvent(String prescriptionId) {
        this.prescriptionId = prescriptionId;
        this.eventType = "PRESCRIPTION_PDF_TASK";
    }

    public String getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(String prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}
