package com.pms.hospitalservice.repository;

import com.pms.hospitalservice.model.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, UUID> {
    Optional<Hospital> findByHospitalId(String hospitalId);
    void deleteByHospitalId(String hospitalId);
    Hospital getHospitalByHospitalId(String hospitalId);
    Optional<Hospital> findFirstByName(String name);
}
