package com.pms.hospitalservice.service;

import com.pms.hospitalservice.dto.response.DepartmentDoctorResponseDTO;
import com.pms.hospitalservice.dto.request.DepartmentRequestDTO;
import com.pms.hospitalservice.dto.response.DepartmentResponseDTO;
import com.pms.hospitalservice.exception.DepartmentNotFoundException;
import com.pms.hospitalservice.service.factory.DepartmentFactory;
import com.pms.hospitalservice.service.factory.DoctorFactory;
import com.pms.hospitalservice.model.Department;
import com.pms.hospitalservice.model.Doctor;
import com.pms.hospitalservice.model.Hospital;
import com.pms.hospitalservice.repository.DepartmentRepository;
import com.pms.hospitalservice.repository.DoctorRepository;
import com.pms.hospitalservice.repository.HospitalRepository;
import com.pms.hospitalservice.service.util.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    private static final Logger log = LoggerFactory.getLogger(DepartmentService.class);

    private final DepartmentRepository departmentRepository;
    private final HospitalRepository hospitalRepository;
    private final DoctorRepository doctorRepository;
    private final IdGenerator idGenerator;

    public DepartmentService(DepartmentRepository departmentRepository, HospitalRepository hospitalRepository, DoctorRepository doctorRepository, IdGenerator idGenerator) {
        this.departmentRepository = departmentRepository;
        this.hospitalRepository = hospitalRepository;
        this.doctorRepository = doctorRepository;
        this.idGenerator = idGenerator;
    }

    public DepartmentResponseDTO createDepartment(DepartmentRequestDTO dto, Hospital hospital) {
        log.debug("Creating department for hospital: {}", hospital.getHospitalId());
        List<Doctor> doctors = new ArrayList<>();
        if (dto.doctors() != null) {
            for (var docDto : dto.doctors()) {
                Doctor doctor = DoctorFactory.createEntity(docDto);
                doctor.setDoctorId(idGenerator.nextId("DOC", "doctor_seq"));
                doctors.add(doctor);
            }
        }
        Department department = DepartmentFactory.createEntity(
                idGenerator.nextId("DEP", "department_seq"),
                hospital,
                dto.name(),
                doctors
        );
        for (Doctor doctor : doctors) {
            doctor.setDepartment(department);
        }
        department = departmentRepository.save(department);
        return DepartmentFactory.toResponseDTO(department);
    }

    public List<DepartmentResponseDTO> getAllDepartments() {
        log.debug("Fetching all departments");
        return departmentRepository.findAll().stream()
                .map(DepartmentFactory::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<DepartmentDoctorResponseDTO> getAllDepartmentsWithDoctors() {
        log.debug("Fetching all departments with doctors");
        return departmentRepository.findAll().stream()
                .map(DepartmentFactory::toDoctorResponseDTO)
                .collect(Collectors.toList());
    }

    public List<DepartmentResponseDTO> getAllDepartmentsByHospitalId(String hospitalId) {
        log.debug("Fetching all departments by hospital id: {}", hospitalId);
        Hospital hospital = hospitalRepository.findByHospitalId(hospitalId)
                .orElseThrow(() -> new RuntimeException("Hospital not found: " + hospitalId));
        return departmentRepository.findByHospital(hospital).stream()
                .map(DepartmentFactory::toResponseDTO)
                .collect(Collectors.toList());
    }

    public DepartmentResponseDTO getDepartmentById(String departmentId) {
        log.debug("Fetching department by id: {}", departmentId);
        Department department = departmentRepository.findByDepartmentId(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found: " + departmentId));
        return DepartmentFactory.toResponseDTO(department);
    }

    @Transactional
    public DepartmentResponseDTO updateDepartment(String departmentId, DepartmentRequestDTO dto) {
        log.debug("Updating department: {}", departmentId);
        Department department = departmentRepository.findByDepartmentId(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found: " + departmentId));
        DepartmentFactory.updateEntity(department, dto);
        department = departmentRepository.save(department);
        return DepartmentFactory.toResponseDTO(department);
    }

    @Transactional
    public void deleteDepartment(String departmentId) {
        log.debug("Deleting department: {}", departmentId);
        Department department = departmentRepository.findByDepartmentId(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found: " + departmentId));
        departmentRepository.delete(department);
    }
}
