package com.pms.timelineservice.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TimelineEntry {
    private String id;
    private String entryId;
    private String appointmentId;
    private String hospitalId;
    private String hospitalName;
    private String doctorId;
    private String doctorName;
    private String appointmentStatus;
    private Instant startedAt;
    private Instant endedAt;
    private List<Map<String, Object>> prescriptions;

    public TimelineEntry() {}

    public static Builder builder() { return new Builder(); }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEntryId() { return entryId; }
    public void setEntryId(String entryId) { this.entryId = entryId; }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getAppointmentStatus() { return appointmentStatus; }
    public void setAppointmentStatus(String appointmentStatus) { this.appointmentStatus = appointmentStatus; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getEndedAt() { return endedAt; }
    public void setEndedAt(Instant endedAt) { this.endedAt = endedAt; }

    public List<Map<String, Object>> getPrescriptions() { return prescriptions; }
    public void setPrescriptions(List<Map<String, Object>> prescriptions) { this.prescriptions = prescriptions; }

    public static class Builder {
        private String id;
        private String entryId;
        private String appointmentId;
        private String hospitalId;
        private String hospitalName;
        private String doctorId;
        private String doctorName;
        private String appointmentStatus;
        private Instant startedAt;
        private Instant endedAt;
        private List<Map<String, Object>> prescriptions;

        Builder() {}

        public Builder id(String id) { this.id = id; return this; }
        public Builder entryId(String entryId) { this.entryId = entryId; return this; }
        public Builder appointmentId(String appointmentId) { this.appointmentId = appointmentId; return this; }
        public Builder hospitalId(String hospitalId) { this.hospitalId = hospitalId; return this; }
        public Builder hospitalName(String hospitalName) { this.hospitalName = hospitalName; return this; }
        public Builder doctorId(String doctorId) { this.doctorId = doctorId; return this; }
        public Builder doctorName(String doctorName) { this.doctorName = doctorName; return this; }
        public Builder appointmentStatus(String appointmentStatus) { this.appointmentStatus = appointmentStatus; return this; }
        public Builder startedAt(Instant startedAt) { this.startedAt = startedAt; return this; }
        public Builder endedAt(Instant endedAt) { this.endedAt = endedAt; return this; }
        public Builder prescriptions(List<Map<String, Object>> prescriptions) { this.prescriptions = prescriptions; return this; }

        public TimelineEntry build() {
            TimelineEntry entry = new TimelineEntry();
            entry.id = this.id;
            entry.entryId = this.entryId;
            entry.appointmentId = this.appointmentId;
            entry.hospitalId = this.hospitalId;
            entry.hospitalName = this.hospitalName;
            entry.doctorId = this.doctorId;
            entry.doctorName = this.doctorName;
            entry.appointmentStatus = this.appointmentStatus;
            entry.startedAt = this.startedAt;
            entry.endedAt = this.endedAt;
            entry.prescriptions = this.prescriptions != null ? this.prescriptions : new ArrayList<>();
            return entry;
        }
    }
}
