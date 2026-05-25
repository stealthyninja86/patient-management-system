package com.pms.clinicalservice.repository;

import com.pms.clinicalservice.model.Drug;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DrugRepository extends JpaRepository<Drug, UUID> {
    Drug findByDrugId(String drugId);
    Drug findByName(String name);
    boolean existsByName(String name);
}
