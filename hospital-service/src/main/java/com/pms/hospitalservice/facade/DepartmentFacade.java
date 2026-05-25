package com.pms.hospitalservice.facade;

import com.pms.hospitalservice.dto.DepartmentDoctorResponseDTO;
import com.pms.hospitalservice.dto.DepartmentRequestDTO;
import com.pms.hospitalservice.dto.DepartmentResponseDTO;
import com.pms.hospitalservice.exception.HospitalNotFoundException;
import com.pms.hospitalservice.model.Hospital;
import com.pms.hospitalservice.repository.DepartmentRepository;
import com.pms.hospitalservice.repository.HospitalRepository;
import com.pms.hospitalservice.service.DepartmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DepartmentFacade {

    private static final Logger log = LoggerFactory.getLogger(DepartmentFacade.class);

    private final DepartmentService departmentService;
    private final DepartmentRepository departmentRepository;
    private final HospitalRepository hospitalRepository;

    public DepartmentFacade(DepartmentService departmentService, DepartmentRepository departmentRepository, HospitalRepository hospitalRepository) {
        this.departmentService = departmentService;
        this.departmentRepository = departmentRepository;
        this.hospitalRepository = hospitalRepository;
    }

    public List<DepartmentResponseDTO> getAllDepartments() {
        log.debug("Facade: getAllDepartments");
        return departmentService.getAllDepartments();
    }

    public List<DepartmentDoctorResponseDTO> getAllDepartmentsWithDoctors() {
        log.debug("Facade: getAllDepartmentsWithDoctors");
        return departmentService.getAllDepartmentsWithDoctors();
    }

    public DepartmentResponseDTO getDepartmentById(String departmentId) {
        log.debug("Facade: getDepartmentById: {}", departmentId);
        return departmentService.getDepartmentById(departmentId);
    }

    public List<DepartmentResponseDTO> getDepartmentsByHospitalId(String hospitalId) {
        log.debug("Facade: getDepartmentsByHospitalId: {}", hospitalId);
        return departmentService.getAllDepartmentsByHospitalId(hospitalId);
    }

    public DepartmentResponseDTO createDepartment(String hospitalId, DepartmentRequestDTO dto) {
        log.debug("Facade: createDepartment for hospital: {}", hospitalId);
        Hospital hospital = hospitalRepository.findByHospitalId(hospitalId)
                .orElseThrow(() -> new HospitalNotFoundException("Hospital not found: " + hospitalId));
        return departmentService.createDepartment(dto, hospital);
    }

    public DepartmentResponseDTO updateDepartment(String departmentId, DepartmentRequestDTO dto) {
        log.debug("Facade: updateDepartment: {}", departmentId);
        return departmentService.updateDepartment(departmentId, dto);
    }

    public void deleteDepartment(String departmentId) {
        log.debug("Facade: deleteDepartment: {}", departmentId);
        departmentService.deleteDepartment(departmentId);
    }
}
