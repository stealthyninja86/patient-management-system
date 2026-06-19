package com.pms.clinicalservice;

import com.pms.clinicalservice.service.factory.PrescriptionAssembler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ClinicalServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClinicalServiceApplication.class, args);
    }

    @Bean
    public PrescriptionAssembler prescriptionFactory() {
        return new PrescriptionAssembler();
    }
}
