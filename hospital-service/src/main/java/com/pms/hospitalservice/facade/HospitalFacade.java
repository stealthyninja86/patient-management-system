package com.pms.hospitalservice.facade;

import com.pms.hospitalservice.dto.HospitalRequestDTO;
import com.pms.hospitalservice.dto.HospitalResponseDTO;
import com.pms.hospitalservice.service.HospitalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HospitalFacade {

    private static final Logger log = LoggerFactory.getLogger(HospitalFacade.class);

    private final HospitalService hospitalService;

    public HospitalFacade(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    public List<HospitalResponseDTO> getAllHospitals() {
        log.debug("Facade: getAllHospitals");
        return hospitalService.getAllHospitals();
    }

    public HospitalResponseDTO getHospitalById(String hospitalId) {
        log.debug("Facade: getHospitalById: {}", hospitalId);
        return hospitalService.getHospitalById(hospitalId);
    }

    public HospitalResponseDTO createHospital(HospitalRequestDTO dto) {
        log.debug("Facade: createHospital");
        return hospitalService.createHospital(dto);
    }

    public HospitalResponseDTO updateHospital(String hospitalId, HospitalRequestDTO dto) {
        log.debug("Facade: updateHospital: {}", hospitalId);
        return hospitalService.updateHospital(hospitalId, dto);
    }

    public void deleteHospital(String hospitalId) {
        log.debug("Facade: deleteHospital: {}", hospitalId);
        hospitalService.deleteHospital(hospitalId);
    }
}
