package com.pms.clinicalservice.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
public class Drug {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true)
    private String drugId;

    private String name;
    private String dosage;
    private String description;
    private String usage;

    @Enumerated(EnumType.STRING)
    private DrugType type;

    @ManyToMany(mappedBy = "drugs")
    private List<Prescription> prescriptions;

    public Drug() {}

    public Drug(UUID id, String drugId, String name, String dosage, String description, String usage, DrugType type, List<Prescription> prescriptions) {
        this.id = id;
        this.drugId = drugId;
        this.name = name;
        this.dosage = dosage;
        this.description = description;
        this.usage = usage;
        this.type = type;
        this.prescriptions = prescriptions;
    }

    public static DrugBuilder builder() {
        return new DrugBuilder();
    }

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
    public String getDrugId() {
        return drugId;
    }
    public void setDrugId(String drugId) {
        this.drugId = drugId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDosage() {
        return dosage;
    }
    public void setDosage(String dosage) {
        this.dosage = dosage;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getUsage() {
        return usage;
    }
    public void setUsage(String usage) {
        this.usage = usage;
    }
    public DrugType getType() {
        return type;
    }
    public void setType(DrugType type) {
        this.type = type;
    }
    public List<Prescription> getPrescriptions() {
        return prescriptions;
    }
    public void setPrescriptions(List<Prescription> prescriptions) {
        this.prescriptions = prescriptions;
    }

    public static class DrugBuilder {
        private UUID id;
        private String drugId;
        private String name;
        private String dosage;
        private String description;
        private String usage;
        private DrugType type;
        private List<Prescription> prescriptions;

        DrugBuilder() {}

        public DrugBuilder id(UUID id) {
            this.id = id;
            return this;
        }
        public DrugBuilder drugId(String drugId) {
            this.drugId = drugId;
            return this;
        }
        public DrugBuilder name(String name) {
            this.name = name;
            return this;
        }
        public DrugBuilder dosage(String dosage) {
            this.dosage = dosage;
            return this;
        }
        public DrugBuilder description(String description) {
            this.description = description;
            return this;
        }
        public DrugBuilder usage(String usage) {
            this.usage = usage;
            return this;
        }
        public DrugBuilder type(DrugType type) {
            this.type = type;
            return this;
        }
        public DrugBuilder prescriptions(List<Prescription> prescriptions) {
            this.prescriptions = prescriptions;
            return this;
        }

        public Drug build() {
            return new Drug(id, drugId, name, dosage, description, usage, type, prescriptions);
        }
    }
}
