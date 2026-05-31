package com.pms.hospitalservice.repository;

import com.pms.hospitalservice.model.Department;
import com.pms.hospitalservice.model.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    Optional<Department> findByDepartmentId(String departmentId);
    List<Department> findByHospital(Hospital hospital);

    @Query("SELECT d FROM Department d JOIN FETCH d.hospital WHERE d.departmentId = :departmentId")
    Optional<Department> findByDepartmentIdWithHospital(@Param("departmentId") String departmentId);
}
