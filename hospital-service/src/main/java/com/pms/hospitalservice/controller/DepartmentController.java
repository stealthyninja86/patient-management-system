package com.pms.hospitalservice.controller;

import com.pms.hospitalservice.dto.response.DepartmentDoctorResponseDTO;
import com.pms.hospitalservice.dto.request.DepartmentRequestDTO;
import com.pms.hospitalservice.dto.response.DepartmentResponseDTO;
import com.pms.hospitalservice.service.facade.DepartmentFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/departments")
public class DepartmentController {

    private static final Logger log = LoggerFactory.getLogger(DepartmentController.class);

    private final DepartmentFacade departmentFacade;

    public DepartmentController(DepartmentFacade departmentFacade) {
        this.departmentFacade = departmentFacade;
    }

    @GetMapping
    public ResponseEntity<List<DepartmentResponseDTO>> getAllDepartments() {
        log.info("GET /departments");
        return ResponseEntity.ok(departmentFacade.getAllDepartments());
    }

    @GetMapping("/with-doctors")
    public ResponseEntity<List<DepartmentDoctorResponseDTO>> getAllDepartmentsWithDoctors() {
        log.info("GET /departments/with-doctors");
        return ResponseEntity.ok(departmentFacade.getAllDepartmentsWithDoctors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponseDTO> getDepartmentById(@PathVariable String id) {
        log.info("GET /departments/{}", id);
        return ResponseEntity.ok(departmentFacade.getDepartmentById(id));
    }

    @PostMapping
    public ResponseEntity<DepartmentResponseDTO> createDepartment(
            @RequestParam String hospitalId,
            @RequestBody DepartmentRequestDTO dto) {
        log.info("POST /departments for hospital: {}", hospitalId);
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentFacade.createDepartment(hospitalId, dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartmentResponseDTO> updateDepartment(@PathVariable String id, @RequestBody DepartmentRequestDTO dto) {
        log.info("PUT /departments/{}", id);
        return ResponseEntity.ok(departmentFacade.updateDepartment(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable String id) {
        log.info("DELETE /departments/{}", id);
        departmentFacade.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
}
