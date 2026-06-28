package com.pms.hospitalservice.service.mapper;

import com.pms.hospitalservice.dto.response.DepartmentResponseDTO;
import com.pms.hospitalservice.dto.request.HospitalRequestDTO;
import com.pms.hospitalservice.dto.response.HospitalResponseDTO;
import com.pms.hospitalservice.model.Hospital;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class HospitalMapper {

    private static final Logger log = LoggerFactory.getLogger(HospitalMapper.class);

    public static Hospital createEntity(HospitalRequestDTO dto) {
        log.debug("Creating hospital entity from DTO");
        return Hospital.builder()
                .name(dto.name())
                .address(dto.address())
                .website(dto.website())
                .email(dto.email())
                .phone(dto.phone())
                .build();
    }

    public static void updateEntity(Hospital existing, HospitalRequestDTO dto) {
        log.debug("Updating hospital entity");
        if (dto.name() != null) {
            existing.setName(dto.name());
        }
        if (dto.address() != null) {
            existing.setAddress(dto.address());
        }
    }

    public static HospitalResponseDTO toResponseDTO(Hospital hospital) {
        log.debug("Converting Hospital to response DTO");
        return new HospitalResponseDTO(
                hospital.getHospitalId(),
                hospital.getName(),
                hospital.getAddress(),
                hospital.getWebsite(),
                hospital.getEmail(),
                hospital.getPhone(),
                List.of()
        );
    }

    public static HospitalResponseDTO toResponseDTO(Hospital hospital, List<DepartmentResponseDTO> departments) {
        log.debug("Converting Hospital to response DTO with departments");
        return new HospitalResponseDTO(
                hospital.getHospitalId(),
                hospital.getName(),
                hospital.getAddress(),
                hospital.getWebsite(),
                hospital.getEmail(),
                hospital.getPhone(),
                departments
        );
    }
}
