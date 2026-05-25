package com.pms.hospitalservice.factory;

import com.pms.hospitalservice.dto.DepartmentDoctorResponseDTO;
import com.pms.hospitalservice.dto.DepartmentRequestDTO;
import com.pms.hospitalservice.dto.DepartmentResponseDTO;
import com.pms.hospitalservice.dto.DoctorResponseDTO;
import com.pms.hospitalservice.model.Department;
import com.pms.hospitalservice.model.Doctor;
import com.pms.hospitalservice.model.Hospital;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class DepartmentFactory {

    private static final Logger log = LoggerFactory.getLogger(DepartmentFactory.class);

    public static Department createEntity(String departmentId, Hospital hospital, String name, List<Doctor> doctors) {
        log.debug("Creating department entity: {}", departmentId);
        return Department.builder()
                .departmentId(departmentId)
                .hospital(hospital)
                .name(name)
                .doctorList(doctors)
                .build();
    }

    public static void updateEntity(Department existing, DepartmentRequestDTO dto) {
        log.debug("Updating department entity");
        if (dto.name() != null) {
            existing.setName(dto.name());
        }
    }

    private static List<DoctorResponseDTO> mapDoctors(Department department) {
        if (department.getDoctorList() == null) {
            return List.of();
        }
        return department.getDoctorList().stream()
                .map(DoctorFactory::toResponseDTO)
                .collect(Collectors.toList());
    }

    public static DepartmentResponseDTO toResponseDTO(Department department) {
        log.debug("Converting Department to response DTO");
        return new DepartmentResponseDTO(
                department.getDepartmentId(),
                department.getName(),
                mapDoctors(department)
        );
    }

    public static DepartmentDoctorResponseDTO toDoctorResponseDTO(Department department) {
        log.debug("Converting Department to doctor response DTO");
        return new DepartmentDoctorResponseDTO(
                department.getDepartmentId(),
                department.getName(),
                department.getHospital() != null ? department.getHospital().getHospitalId() : null,
                department.getHospital() != null ? department.getHospital().getName() : null,
                mapDoctors(department)
        );
    }
}
