package com.pms.hospitalservice.service;

import com.pms.hospitalservice.dto.DepartmentRequestDTO;
import com.pms.hospitalservice.dto.DepartmentResponseDTO;
import com.pms.hospitalservice.dto.HospitalRequestDTO;
import com.pms.hospitalservice.dto.HospitalResponseDTO;
import com.pms.hospitalservice.exception.HospitalNotFoundException;
import com.pms.hospitalservice.dto.DoctorRequestDTO;
import com.pms.hospitalservice.factory.DepartmentFactory;
import com.pms.hospitalservice.factory.DoctorFactory;
import com.pms.hospitalservice.factory.HospitalFactory;
import com.pms.hospitalservice.model.Department;
import com.pms.hospitalservice.model.Doctor;
import com.pms.hospitalservice.model.Hospital;
import com.pms.hospitalservice.repository.HospitalRepository;
import com.pms.hospitalservice.util.IdGenerator;
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
        Hospital hospital = HospitalFactory.createEntity(dto);
        String id = idGenerator.nextId("HMC", "hospital_seq");
        hospital.setHospitalId(id);

        if (dto.departments() != null && !dto.departments().isEmpty()) {
            List<Department> departments = new ArrayList<>();
            for (DepartmentRequestDTO deptDto : dto.departments()) {
                List<Doctor> doctors = new ArrayList<>();
                if (deptDto.doctors() != null) {
                    for (DoctorRequestDTO docDto : deptDto.doctors()) {
                        Doctor doctor = DoctorFactory.createEntity(docDto);
                        doctor.setDoctorId(idGenerator.nextId("DOC", "doctor_seq"));
                        doctors.add(doctor);
                    }
                }
                Department department = DepartmentFactory.createEntity(
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
        return HospitalFactory.toResponseDTO(hospital);
    }

    public List<HospitalResponseDTO> getAllHospitals() {
        log.debug("Fetching all hospitals");
        return hospitalRepository.findAll().stream()
                .map(hospital -> {
                    List<DepartmentResponseDTO> deptDTOs = hospital.getDepartmentList() != null
                            ? hospital.getDepartmentList().stream()
                                .map(DepartmentFactory::toResponseDTO)
                                .collect(Collectors.toList())
                            : List.of();
                    return HospitalFactory.toResponseDTO(hospital, deptDTOs);
                })
                .collect(Collectors.toList());
    }

    public HospitalResponseDTO getHospitalById(String hospitalId) {
        log.debug("Fetching hospital by id: {}", hospitalId);
        Hospital hospital = hospitalRepository.findByHospitalId(hospitalId)
                .orElseThrow(() -> new HospitalNotFoundException("Hospital not found: " + hospitalId));
        List<DepartmentResponseDTO> deptDTOs = hospital.getDepartmentList() != null
                ? hospital.getDepartmentList().stream()
                    .map(DepartmentFactory::toResponseDTO)
                    .collect(Collectors.toList())
                : List.of();
        return HospitalFactory.toResponseDTO(hospital, deptDTOs);
    }

    @Transactional
    public HospitalResponseDTO updateHospital(String hospitalId, HospitalRequestDTO dto) {
        log.debug("Updating hospital: {}", hospitalId);
        Hospital hospital = hospitalRepository.findByHospitalId(hospitalId)
                .orElseThrow(() -> new HospitalNotFoundException("Hospital not found: " + hospitalId));
        HospitalFactory.updateEntity(hospital, dto);
        hospital = hospitalRepository.save(hospital);
        return HospitalFactory.toResponseDTO(hospital);
    }

    @Transactional
    public void deleteHospital(String hospitalId) {
        log.debug("Deleting hospital: {}", hospitalId);
        hospitalRepository.deleteByHospitalId(hospitalId);
    }
}
