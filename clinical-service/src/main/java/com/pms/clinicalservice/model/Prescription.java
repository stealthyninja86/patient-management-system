package com.pms.clinicalservice.model;

import com.pms.clinicalservice.exception.InvalidPrescriptionOperationException;
import jakarta.persistence.*;
import java.time.LocalDate;
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
    private String patientPhone;
    private String patientEmail;
    private String patientGender;
    private LocalDate patientDateOfBirth;
    private String doctorEmail;
    private String doctorPhone;
    private String hospitalPhone;
    private String hospitalEmail;
    private String hospitalWebsite;
    private String hospitalAddress;
    private int followUpWeeks;
    private String doctorNote;

    @Column(unique = true)
    private String idempotencyKey;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "prescription_drugs")
    private List<Drug> drugs;

    @Enumerated(EnumType.STRING)
    private PrescriptionStatus status;

    protected Prescription() {}

    public Prescription(UUID id, String prescriptionId, String doctorId, String doctorName,
                        String departmentId, String departmentName, String hospitalId,
                        String hospitalName, String patientId, String patientName,
                        String diagnosis, int painScore, String allergies,
                        LocalDateTime consultationDate, List<Drug> drugs, PrescriptionStatus status,
                         String patientPhone, String patientEmail, String patientGender, LocalDate patientDateOfBirth,
                        String doctorEmail, String doctorPhone, String hospitalEmail, String hospitalPhone,
                         String hospitalWebsite, String hospitalAddress, int followUpWeeks,
                        String idempotencyKey, String doctorNote) {
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
        this.patientPhone = patientPhone;
        this.patientEmail = patientEmail;
        this.patientGender = patientGender;
        this.patientDateOfBirth = patientDateOfBirth;
        this.doctorEmail = doctorEmail;
        this.doctorPhone = doctorPhone;
        this.hospitalPhone = hospitalPhone;
        this.hospitalEmail = hospitalEmail;
        this.hospitalWebsite = hospitalWebsite;
        this.hospitalAddress = hospitalAddress;
        this.followUpWeeks = followUpWeeks;
        this.idempotencyKey = idempotencyKey;
        this.doctorNote = doctorNote;
    }

    public static PrescriptionBuilder builder() {
        return new PrescriptionBuilder();
    }

    public UUID getId() {
        return id;
    }
    public String getPrescriptionId() {
        return prescriptionId;
    }
    public void setPrescriptionId(String prescriptionId) {
        this.prescriptionId = prescriptionId;
    }
    public String getDoctorId() {
        return doctorId;
    }
    public String getDoctorName() {
        return doctorName;
    }
    public String getDepartmentId() {
        return departmentId;
    }
    public String getDepartmentName() {
        return departmentName;
    }
    public String getHospitalId() {
        return hospitalId;
    }
    public String getHospitalName() {
        return hospitalName;
    }
    public String getPatientId() {
        return patientId;
    }
    public String getPatientName() {
        return patientName;
    }
    public String getDiagnosis() {
        return diagnosis;
    }
    public int getPainScore() {
        return painScore;
    }
    public String getAllergies() {
        return allergies;
    }
    public LocalDateTime getConsultationDate() {
        return consultationDate;
    }
    public List<Drug> getDrugs() {
        return drugs;
    }
    public PrescriptionStatus getStatus() {
        return status;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public int getFollowUpWeeks() {
        return followUpWeeks;
    }

    public String getHospitalWebsite() {
        return hospitalWebsite;
    }

    public void setHospitalWebsite(String hospitalWebsite) {
        this.hospitalWebsite = hospitalWebsite;
    }

    public String getHospitalAddress() {
        return hospitalAddress;
    }

    public String getHospitalEmail() {
        return hospitalEmail;
    }

    public void setHospitalEmail(String hospitalEmail) {
        this.hospitalEmail = hospitalEmail;
    }

    public String getHospitalPhone() {
        return hospitalPhone;
    }

    public void setHospitalPhone(String hospitalPhone) {
        this.hospitalPhone = hospitalPhone;
    }

    public String getDoctorPhone() {
        return doctorPhone;
    }

    public void setDoctorPhone(String doctorPhone) {
        this.doctorPhone = doctorPhone;
    }

    public String getDoctorEmail() {
        return doctorEmail;
    }

    public void setDoctorEmail(String doctorEmail) {
        this.doctorEmail = doctorEmail;
    }

    public LocalDate getPatientDateOfBirth() {
        return patientDateOfBirth;
    }

    public String getPatientGender() {
        return patientGender;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getDoctorNote() {
        return doctorNote;
    }

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
        private String patientPhone;
        private String patientEmail;
        private String patientGender;
        private LocalDate patientDateOfBirth;
        private String doctorEmail;
        private String doctorPhone;
        private String hospitalPhone;
        private String hospitalEmail;
        private String hospitalWebsite;
        private String hospitalAddress;
        private int followUpWeeks;
        private String idempotencyKey;
        private String doctorNote;

        PrescriptionBuilder() {}

        public PrescriptionBuilder id(UUID id) {
            this.id = id;
            return this;
        }
        public PrescriptionBuilder prescriptionId(String prescriptionId) {
            this.prescriptionId = prescriptionId;
            return this;
        }
        public PrescriptionBuilder doctorId(String doctorId) {
            this.doctorId = doctorId;
            return this;
        }
        public PrescriptionBuilder doctorName(String doctorName) {
            this.doctorName = doctorName;
            return this;
        }
        public PrescriptionBuilder departmentId(String departmentId) {
            this.departmentId = departmentId;
            return this;
        }
        public PrescriptionBuilder departmentName(String departmentName) {
            this.departmentName = departmentName;
            return this;
        }
        public PrescriptionBuilder hospitalId(String hospitalId) {
            this.hospitalId = hospitalId;
            return this;
        }
        public PrescriptionBuilder hospitalName(String hospitalName) {
            this.hospitalName = hospitalName;
            return this;
        }
        public PrescriptionBuilder patientId(String patientId) {
            this.patientId = patientId;
            return this;
        }
        public PrescriptionBuilder patientName(String patientName) {
            this.patientName = patientName;
            return this;
        }
        public PrescriptionBuilder diagnosis(String diagnosis) {
            this.diagnosis = diagnosis;
            return this;
        }
        public PrescriptionBuilder painScore(int painScore) {
            this.painScore = painScore;
            return this;
        }
        public PrescriptionBuilder allergies(String allergies) {
            this.allergies = allergies;
            return this;
        }
        public PrescriptionBuilder consultationDate(LocalDateTime consultationDate) {
            this.consultationDate = consultationDate;
            return this;
        }
        public PrescriptionBuilder drugs(List<Drug> drugs) {
            this.drugs = drugs;
            return this;
        }
        public PrescriptionBuilder status(PrescriptionStatus status) {
            this.status = status;
            return this;
        }
        public PrescriptionBuilder patientPhone(String patientPhone) {
            this.patientPhone = patientPhone;
            return this;
        }
        public PrescriptionBuilder patientEmail(String patientEmail) {
            this.patientEmail = patientEmail;
            return this;
        }
        public PrescriptionBuilder patientGender(String patientGender) {
            this.patientGender = patientGender;
            return this;
        }
        public PrescriptionBuilder patientDateOfBirth(LocalDate patientDateOfBirth) {
            this.patientDateOfBirth = patientDateOfBirth;
            return this;
        }
        public PrescriptionBuilder doctorEmail(String doctorEmail) {
            this.doctorEmail = doctorEmail;
            return this;
        }
        public PrescriptionBuilder doctorPhone(String doctorPhone) {
            this.doctorPhone = doctorPhone;
            return this;
        }
        public PrescriptionBuilder hospitalPhone(String hospitalPhone) {
            this.hospitalPhone = hospitalPhone;
            return this;
        }
        public PrescriptionBuilder hospitalEmail(String hospitalEmail) {
            this.hospitalEmail = hospitalEmail;
            return this;
        }
        public PrescriptionBuilder hospitalWebsite(String hospitalWebsite) {
            this.hospitalWebsite = hospitalWebsite;
            return this;
        }
        public PrescriptionBuilder hospitalAddress(String hospitalAddress) {
            this.hospitalAddress = hospitalAddress;
            return this;
        }
        public PrescriptionBuilder followUpWeeks(int followUpWeeks) {
            this.followUpWeeks = followUpWeeks;
            return this;
        }
        public PrescriptionBuilder idempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }
        public PrescriptionBuilder doctorNote(String doctorNote) {
            this.doctorNote = doctorNote;
            return this;
        }

        public Prescription build() {
            if (doctorNote == null || doctorNote.isBlank()) {
                throw new InvalidPrescriptionOperationException("Doctor's note is required");
            }
            if (drugs != null) {
                for (Drug drug : drugs) {
                    if (drug.getDosage() == null || drug.getDosage().isBlank()) {
                        throw new InvalidPrescriptionOperationException("Dosage is required for drug: " + drug.getName());
                    }
                }
            }
            return new Prescription(id, prescriptionId, doctorId, doctorName, departmentId, departmentName,
                    hospitalId, hospitalName, patientId, patientName, diagnosis, painScore, allergies,
                    consultationDate, drugs, status, patientPhone, patientEmail, patientGender, patientDateOfBirth,
                    doctorEmail, doctorPhone, hospitalPhone, hospitalEmail, hospitalWebsite, hospitalAddress, followUpWeeks,
                    idempotencyKey, doctorNote);
        }
    }
}
