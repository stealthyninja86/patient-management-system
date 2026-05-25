package com.pms.clinicalservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true)
    private String prescriptionId;

    private String doctorId;
    private String doctorName;
    private String departmentId;
    private String departmentName;
    private String hospitalId;
    private String hospitalName;
    private String patientId;
    private String patientName;
    private String diagnosis;
    private int painScore;
    private String allergies;
    private LocalDateTime consultationDate;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "prescription_drugs")
    private List<Drug> drugs;

    @Enumerated(EnumType.STRING)
    private PrescriptionStatus status;

    public Prescription() {}

    public Prescription(UUID id, String prescriptionId, String doctorId, String doctorName,
                        String departmentId, String departmentName, String hospitalId,
                        String hospitalName, String patientId, String patientName,
                        String diagnosis, int painScore, String allergies,
                        LocalDateTime consultationDate, List<Drug> drugs, PrescriptionStatus status) {
        this.id = id;
        this.prescriptionId = prescriptionId;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.hospitalId = hospitalId;
        this.hospitalName = hospitalName;
        this.patientId = patientId;
        this.patientName = patientName;
        this.diagnosis = diagnosis;
        this.painScore = painScore;
        this.allergies = allergies;
        this.consultationDate = consultationDate;
        this.drugs = drugs;
        this.status = status;
    }

    public static PrescriptionBuilder builder() {
        return new PrescriptionBuilder();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(String prescriptionId) { this.prescriptionId = prescriptionId; }
    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }
    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public int getPainScore() { return painScore; }
    public void setPainScore(int painScore) { this.painScore = painScore; }
    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }
    public LocalDateTime getConsultationDate() { return consultationDate; }
    public void setConsultationDate(LocalDateTime consultationDate) { this.consultationDate = consultationDate; }
    public List<Drug> getDrugs() { return drugs; }
    public void setDrugs(List<Drug> drugs) { this.drugs = drugs; }
    public PrescriptionStatus getStatus() { return status; }
    public void setStatus(PrescriptionStatus status) { this.status = status; }

    public static class PrescriptionBuilder {
        private UUID id;
        private String prescriptionId;
        private String doctorId;
        private String doctorName;
        private String departmentId;
        private String departmentName;
        private String hospitalId;
        private String hospitalName;
        private String patientId;
        private String patientName;
        private String diagnosis;
        private int painScore;
        private String allergies;
        private LocalDateTime consultationDate;
        private List<Drug> drugs;
        private PrescriptionStatus status;

        PrescriptionBuilder() {}

        public PrescriptionBuilder id(UUID id) { this.id = id; return this; }
        public PrescriptionBuilder prescriptionId(String prescriptionId) { this.prescriptionId = prescriptionId; return this; }
        public PrescriptionBuilder doctorId(String doctorId) { this.doctorId = doctorId; return this; }
        public PrescriptionBuilder doctorName(String doctorName) { this.doctorName = doctorName; return this; }
        public PrescriptionBuilder departmentId(String departmentId) { this.departmentId = departmentId; return this; }
        public PrescriptionBuilder departmentName(String departmentName) { this.departmentName = departmentName; return this; }
        public PrescriptionBuilder hospitalId(String hospitalId) { this.hospitalId = hospitalId; return this; }
        public PrescriptionBuilder hospitalName(String hospitalName) { this.hospitalName = hospitalName; return this; }
        public PrescriptionBuilder patientId(String patientId) { this.patientId = patientId; return this; }
        public PrescriptionBuilder patientName(String patientName) { this.patientName = patientName; return this; }
        public PrescriptionBuilder diagnosis(String diagnosis) { this.diagnosis = diagnosis; return this; }
        public PrescriptionBuilder painScore(int painScore) { this.painScore = painScore; return this; }
        public PrescriptionBuilder allergies(String allergies) { this.allergies = allergies; return this; }
        public PrescriptionBuilder consultationDate(LocalDateTime consultationDate) { this.consultationDate = consultationDate; return this; }
        public PrescriptionBuilder drugs(List<Drug> drugs) { this.drugs = drugs; return this; }
        public PrescriptionBuilder status(PrescriptionStatus status) { this.status = status; return this; }

        public Prescription build() {
            return new Prescription(id, prescriptionId, doctorId, doctorName, departmentId, departmentName,
                    hospitalId, hospitalName, patientId, patientName, diagnosis, painScore, allergies,
                    consultationDate, drugs, status);
        }
    }
}
