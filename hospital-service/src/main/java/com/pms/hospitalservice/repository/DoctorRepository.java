package com.pms.hospitalservice.repository;

import com.pms.hospitalservice.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {
    List<Doctor> findByDepartment_DepartmentId(String departmentId);
    Optional<Doctor> findByDoctorId(String doctorId);
    Doctor getDoctorByDoctorId(String doctorId);

    @Query("SELECT d FROM Doctor d LEFT JOIN FETCH d.department WHERE d.doctorId = :doctorId")
    Optional<Doctor> findByDoctorIdWithDepartment(@Param("doctorId") String doctorId);
}
