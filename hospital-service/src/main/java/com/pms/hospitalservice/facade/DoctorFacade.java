package com.pms.hospitalservice.facade;

import com.pms.hospitalservice.dto.DoctorRequestDTO;
import com.pms.hospitalservice.dto.DoctorResponseDTO;
import com.pms.hospitalservice.service.DoctorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class DoctorFacade {

    private static final Logger log = LoggerFactory.getLogger(DoctorFacade.class);

    private final DoctorService doctorService;

    public DoctorFacade(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    public List<DoctorResponseDTO> getAllDoctors() {
        log.debug("Facade: getAllDoctors");
        return doctorService.getAllDoctors();
    }

    public DoctorResponseDTO getDoctorById(UUID id) {
        log.debug("Facade: getDoctorById: {}", id);
        return doctorService.getDoctorById(id);
    }

    public DoctorResponseDTO getDoctorByDoctorId(String doctorId) {
        log.debug("Facade: getDoctorByDoctorId: {}", doctorId);
        return doctorService.getDoctorByDoctorId(doctorId);
    }

    public List<DoctorResponseDTO> getDoctorsByDepartment(String departmentId) {
        log.debug("Facade: getDoctorsByDepartment: {}", departmentId);
        return doctorService.getDoctorsByDepartment(departmentId);
    }

    public DoctorResponseDTO createDoctor(DoctorRequestDTO dto) {
        log.debug("Facade: createDoctor");
        return doctorService.createDoctor(dto);
    }

    public DoctorResponseDTO updateDoctor(UUID id, DoctorRequestDTO dto) {
        log.debug("Facade: updateDoctor: {}", id);
        return doctorService.updateDoctor(id, dto);
    }

    public void deleteDoctor(UUID id) {
        log.debug("Facade: deleteDoctor: {}", id);
        doctorService.deleteDoctor(id);
    }

    public void deleteDoctorByDoctorId(String doctorId) {
        log.debug("Facade: deleteDoctorByDoctorId: {}", doctorId);
        doctorService.deleteDoctorByDoctorId(doctorId);
    }
}
