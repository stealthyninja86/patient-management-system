package com.pms.hospitalservice.controller;

import com.pms.hospitalservice.dto.request.HospitalRequestDTO;
import com.pms.hospitalservice.dto.response.HospitalResponseDTO;
import com.pms.hospitalservice.service.facade.HospitalFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hospitals")
public class HospitalController {

    private static final Logger log = LoggerFactory.getLogger(HospitalController.class);

    private final HospitalFacade hospitalFacade;

    public HospitalController(HospitalFacade hospitalFacade) {
        this.hospitalFacade = hospitalFacade;
    }

    @GetMapping
    public ResponseEntity<List<HospitalResponseDTO>> getAllHospitals() {
        log.info("GET /hospitals");
        return ResponseEntity.ok(hospitalFacade.getAllHospitals());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HospitalResponseDTO> getHospitalById(@PathVariable String id) {
        log.info("GET /hospitals/{}", id);
        return ResponseEntity.ok(hospitalFacade.getHospitalById(id));
    }

    @PostMapping
    public ResponseEntity<HospitalResponseDTO> createHospital(@RequestBody HospitalRequestDTO dto) {
        log.info("POST /hospitals");
        return ResponseEntity.status(HttpStatus.CREATED).body(hospitalFacade.createHospital(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HospitalResponseDTO> updateHospital(@PathVariable String id, @RequestBody HospitalRequestDTO dto) {
        log.info("PUT /hospitals/{}", id);
        return ResponseEntity.ok(hospitalFacade.updateHospital(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHospital(@PathVariable String id) {
        log.info("DELETE /hospitals/{}", id);
        hospitalFacade.deleteHospital(id);
        return ResponseEntity.noContent().build();
    }
}
