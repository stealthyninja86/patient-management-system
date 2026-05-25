package com.pms.clinicalservice;

import com.pms.clinicalservice.factory.PrescriptionFactory;
import com.pms.clinicalservice.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ClinicalServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClinicalServiceApplication.class, args);
    }

    @Bean
    public JwtUtil jwtUtil(@Value("${jwt.secret}") String secret) {
        return new JwtUtil(secret);
    }

    @Bean
    public PrescriptionFactory prescriptionFactory() {
        return new PrescriptionFactory();
    }
}
