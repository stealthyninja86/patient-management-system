package com.pms.hospitalservice.service;

import com.pms.hospitalservice.dto.request.DepartmentRequestDTO;
import com.pms.hospitalservice.dto.response.DepartmentResponseDTO;
import com.pms.hospitalservice.dto.request.HospitalRequestDTO;
import com.pms.hospitalservice.dto.response.HospitalResponseDTO;
import com.pms.hospitalservice.exception.HospitalNotFoundException;
import com.pms.hospitalservice.dto.request.DoctorRequestDTO;
import com.pms.hospitalservice.service.factory.DepartmentMapper;
import com.pms.hospitalservice.service.factory.DoctorMapper;
import com.pms.hospitalservice.service.factory.HospitalMapper;
import com.pms.hospitalservice.model.Department;
import com.pms.hospitalservice.model.Doctor;
import com.pms.hospitalservice.model.Hospital;
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
public class HospitalService {

    private static final Logger log = LoggerFactory.getLogger(HospitalService.class);

    private final HospitalRepository hospitalRepository;
    private final IdGenerator idGenerator;

    public HospitalService(HospitalRepository hospitalRepository, IdGenerator idGenerator) {
        this.hospitalRepository = hospitalRepository;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public HospitalResponseDTO createHospital(HospitalRequestDTO dto) {
        log.debug("Creating hospital");
        Hospital hospital = HospitalMapper.createEntity(dto);
        String id = idGenerator.nextId("HMC", "hospital_seq");
        hospital.setHospitalId(id);

        if (dto.departments() != null && !dto.departments().isEmpty()) {
            List<Department> departments = new ArrayList<>();
            for (DepartmentRequestDTO deptDto : dto.departments()) {
                List<Doctor> doctors = new ArrayList<>();
                if (deptDto.doctors() != null) {
                    for (DoctorRequestDTO docDto : deptDto.doctors()) {
                        Doctor doctor = DoctorMapper.createEntity(docDto);
                        doctor.setDoctorId(idGenerator.nextId("DOC", "doctor_seq"));
                        doctors.add(doctor);
                    }
                }
                Department department = DepartmentMapper.createEntity(
                        idGenerator.nextId("DEP", "department_seq"),
                        hospital,
                        deptDto.name(),
                        doctors
                );
                for (Doctor doctor : doctors) {
                    doctor.setDepartment(department);
                }
                departments.add(department);
            }
            hospital.setDepartmentList(departments);
        }

        hospital = hospitalRepository.save(hospital);
        List<DepartmentResponseDTO> deptDTOs = hospital.getDepartmentList() != null
                ? hospital.getDepartmentList().stream()
                    .map(DepartmentMapper::toResponseDTO)
                    .collect(Collectors.toList())
                : List.of();
        return HospitalMapper.toResponseDTO(hospital, deptDTOs);
    }

    public List<HospitalResponseDTO> getAllHospitals() {
        log.debug("Fetching all hospitals");
        return hospitalRepository.findAll().stream()
                .map(hospital -> {
                    List<DepartmentResponseDTO> deptDTOs = hospital.getDepartmentList() != null
                            ? hospital.getDepartmentList().stream()
                                .map(DepartmentMapper::toResponseDTO)
                                .collect(Collectors.toList())
                            : List.of();
                    return HospitalMapper.toResponseDTO(hospital, deptDTOs);
                })
                .collect(Collectors.toList());
    }

    public HospitalResponseDTO getHospitalById(String hospitalId) {
        log.debug("Fetching hospital by id: {}", hospitalId);
        Hospital hospital = hospitalRepository.findByHospitalId(hospitalId)
                .orElseThrow(() -> new HospitalNotFoundException("Hospital not found: " + hospitalId));
        List<DepartmentResponseDTO> deptDTOs = hospital.getDepartmentList() != null
                ? hospital.getDepartmentList().stream()
                    .map(DepartmentMapper::toResponseDTO)
                    .collect(Collectors.toList())
                : List.of();
        return HospitalMapper.toResponseDTO(hospital, deptDTOs);
    }

    @Transactional
    public HospitalResponseDTO updateHospital(String hospitalId, HospitalRequestDTO dto) {
        log.debug("Updating hospital: {}", hospitalId);
        Hospital hospital = hospitalRepository.findByHospitalId(hospitalId)
                .orElseThrow(() -> new HospitalNotFoundException("Hospital not found: " + hospitalId));
        HospitalMapper.updateEntity(hospital, dto);
        hospital = hospitalRepository.save(hospital);
        return HospitalMapper.toResponseDTO(hospital);
    }

    @Transactional
    public void deleteHospital(String hospitalId) {
        log.debug("Deleting hospital: {}", hospitalId);
        hospitalRepository.deleteByHospitalId(hospitalId);
    }
}
