package com.pms.hospitalservice.service;

import com.pms.hospitalservice.dto.request.DoctorRequestDTO;
import com.pms.hospitalservice.dto.response.DoctorResponseDTO;
import com.pms.hospitalservice.exception.DepartmentNotFoundException;
import com.pms.hospitalservice.exception.DoctorNotFoundException;
import com.pms.hospitalservice.service.factory.DoctorMapper;
import com.pms.hospitalservice.model.Department;
import com.pms.hospitalservice.model.Doctor;
import com.pms.hospitalservice.repository.DepartmentRepository;
import com.pms.hospitalservice.repository.DoctorRepository;
import com.pms.hospitalservice.repository.HospitalRepository;
import com.pms.hospitalservice.service.util.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    private static final Logger log = LoggerFactory.getLogger(DoctorService.class);
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final HospitalRepository hospitalRepository;
    private final IdGenerator idGenerator;

    public DoctorService(DoctorRepository doctorRepository, DepartmentRepository departmentRepository, HospitalRepository hospitalRepository, IdGenerator idGenerator) {
        this.doctorRepository = doctorRepository;
        this.departmentRepository = departmentRepository;
        this.hospitalRepository = hospitalRepository;
        this.idGenerator = idGenerator;
    }

    private Department getDepartmentFromDoctor(DoctorRequestDTO dto) {
        if (dto.departmentId() != null) {
            return departmentRepository.findByDepartmentId(dto.departmentId())
                    .orElseThrow(() -> {
                        log.warn("Department not found by departmentId: {}", dto.departmentId());
                        return new DepartmentNotFoundException("Department not found: " + dto.departmentId());
                    });
        }
        if (dto.departmentName() != null && dto.hospitalName() != null) {
            return hospitalRepository.findFirstByName(dto.hospitalName())
                    .flatMap(hospital -> departmentRepository.findByHospital(hospital).stream()
                            .filter(d -> d.getName().equals(dto.departmentName()))
                            .findFirst())
                    .orElseThrow(() -> {
                        log.warn("Department not found by name: {} at hospital: {}", dto.departmentName(), dto.hospitalName());
                        return new DepartmentNotFoundException("Department not found: " + dto.departmentName() + " at hospital: " + dto.hospitalName());
                    });
        }
        throw new IllegalArgumentException("Either departmentId or departmentName+hospitalName must be provided");
    }

    public List<DoctorResponseDTO> getAllDoctors() {
        log.debug("Fetching all doctors");
        return doctorRepository.findAll().stream()
                .map(DoctorMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public DoctorResponseDTO getDoctorById(UUID id) {
        log.debug("Fetching doctor by id: {}", id);
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found: " + id));
        return DoctorMapper.toResponseDTO(doctor);
    }

    public DoctorResponseDTO getDoctorByDoctorId(String doctorId) {
        log.debug("Fetching doctor by doctorId: {}", doctorId);
        Doctor doctor = doctorRepository.findByDoctorId(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found: " + doctorId));
        return DoctorMapper.toResponseDTO(doctor);
    }

    public List<DoctorResponseDTO> getDoctorsByDepartment(String departmentId) {
        log.debug("Fetching doctors by department: {}", departmentId);
        return doctorRepository.findByDepartment_DepartmentId(departmentId).stream()
                .map(DoctorMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public DoctorResponseDTO createDoctor(DoctorRequestDTO dto) {
        log.debug("Creating doctor");
        Doctor doctor = DoctorMapper.createEntity(dto);
        doctor.setDepartment(getDepartmentFromDoctor(dto));
        doctor.setDoctorId(idGenerator.nextId("DOC", "doctor_seq"));
        doctor = doctorRepository.save(doctor);
        return DoctorMapper.toResponseDTO(doctor);
    }

    @Transactional
    public DoctorResponseDTO updateDoctor(UUID id, DoctorRequestDTO dto) {
        log.debug("Updating doctor: {}", id);
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found: " + id));
        DoctorMapper.updateEntity(doctor, dto, getDepartmentFromDoctor(dto));
        doctor = doctorRepository.save(doctor);
        return DoctorMapper.toResponseDTO(doctor);
    }

    @Transactional
    public void deleteDoctor(UUID id) {
        log.debug("Deleting doctor: {}", id);
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found: " + id));
        doctorRepository.delete(doctor);
    }

    @Transactional
    public void deleteDoctorByDoctorId(String doctorId) {
        log.debug("Deleting doctor by doctorId: {}", doctorId);
        Doctor doctor = doctorRepository.findByDoctorId(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found: " + doctorId));
        doctorRepository.delete(doctor);
    }
}
