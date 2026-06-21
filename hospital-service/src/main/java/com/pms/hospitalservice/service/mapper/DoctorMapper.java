package com.pms.hospitalservice.service.mapper;

import com.pms.hospitalservice.dto.request.DoctorRequestDTO;
import com.pms.hospitalservice.dto.response.DoctorResponseDTO;
import com.pms.hospitalservice.model.Department;
import com.pms.hospitalservice.model.Doctor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoctorMapper {

    private static final Logger log = LoggerFactory.getLogger(DoctorMapper.class);

    public static Doctor createEntity(DoctorRequestDTO dto) {
        log.debug("Creating doctor entity");
        return Doctor.builder()
                .name(dto.name())
                .email(dto.email())
                .phone(dto.phone())
                .build();
    }

    public static void updateEntity(Doctor existing, DoctorRequestDTO dto, Department department) {
        log.debug("Updating doctor entity");
        if (dto.name() != null) {
            existing.setName(dto.name());
        }
        if (dto.email() != null) {
            existing.setEmail(dto.email());
        }
        if (dto.phone() != null) {
            existing.setPhone(dto.phone());
        }
        if (department != null) {
            existing.setDepartment(department);
        }
    }

    public static DoctorResponseDTO toResponseDTO(Doctor doctor) {
        log.debug("Converting Doctor to response DTO");
        return new DoctorResponseDTO(
                doctor.getDoctorId(),
                doctor.getName(),
                doctor.getEmail(),
                doctor.getPhone(),
                doctor.getDepartment() != null ? doctor.getDepartment().getDepartmentId() : null,
                doctor.getDepartment() != null ? doctor.getDepartment().getName() : null
        );
    }
}
