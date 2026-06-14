package com.pms.clinicalservice.service.pdf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;

@Component
public class AgeCalculator {

    private static final Logger log = LoggerFactory.getLogger(AgeCalculator.class);

    public String calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            log.error("Date of birth is null");
            return "";
        }
        try {
            Period period = Period.between(dateOfBirth, LocalDate.now());
            if (period.isNegative()) {
                log.error("Date of birth is negative");
                return "";
            }
            return String.format("%dy %dm %dd", period.getYears(), period.getMonths(), period.getDays());
        } catch (Exception e) {
            log.warn("Failed to calculate age for DOB: {}", dateOfBirth);
            return "N/A";
        }
    }
}
