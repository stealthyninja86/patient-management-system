package com.pms.authservice.service;

import com.pms.authservice.dto.request.AdminRegisterRequestDTO;
import com.pms.authservice.dto.request.DoctorRegisterRequestDTO;
import com.pms.authservice.dto.request.PatientRegisterRequestDTO;
import com.pms.authservice.dto.response.AdminRegisterResponseDTO;
import com.pms.authservice.dto.response.DepartmentDTO;
import com.pms.authservice.dto.response.DoctorRegisterResponseDTO;
import com.pms.authservice.dto.response.HospitalDTO;
import com.pms.authservice.dto.response.PatientRegisterResponseDTO;
import com.pms.authservice.grpc.HospitalGrpcClient;
import com.pms.authservice.model.Role;
import com.pms.authservice.service.strategy.RegistrationStrategy;
import com.pms.authservice.service.strategy.RegistrationStrategyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final HospitalGrpcClient hospitalGrpcClient;
    private final RegistrationStrategyProvider strategyProvider;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder,
                       HospitalGrpcClient hospitalGrpcClient,
                       RegistrationStrategyProvider strategyProvider) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.hospitalGrpcClient = hospitalGrpcClient;
        this.strategyProvider = strategyProvider;
    }


    public List<HospitalDTO> getAllHospitals() {
        logger.debug("Fetching all hospitals");
        return hospitalGrpcClient.getAllHospitals().stream()
                .map(h -> new HospitalDTO(h.getHospitalId(), h.getName(), h.getAddress()))
                .toList();
    }

    public List<DepartmentDTO> getAllDepartments(String hospitalId) {
        logger.debug("Fetching all departments for hospitalId: {}", hospitalId);
        return hospitalGrpcClient.getAllDepartments(hospitalId).stream()
                .map(d -> new DepartmentDTO(d.getDepartmentId(), d.getName(), d.getHospitalId()))
                .toList();
    }

    @Transactional
    public AdminRegisterResponseDTO registerAdmin(AdminRegisterRequestDTO request) {
        logger.debug("Registering admin with email: {}", request.email());
        String encodedPassword = passwordEncoder.encode(request.password());
        RegistrationStrategy<AdminRegisterRequestDTO, AdminRegisterResponseDTO> strategy = strategyProvider.getStrategy(Role.ADMIN);
        return strategy.register(request, encodedPassword);
    }

    @Transactional
    public DoctorRegisterResponseDTO registerDoctor(DoctorRegisterRequestDTO request) {
        logger.debug("Registering doctor with email: {}", request.email());
        String encodedPassword = passwordEncoder.encode(request.password());
        RegistrationStrategy<DoctorRegisterRequestDTO, DoctorRegisterResponseDTO> strategy = strategyProvider.getStrategy(Role.DOCTOR);
        return strategy.register(request, encodedPassword);
    }

    @Transactional
    public PatientRegisterResponseDTO registerPatient(PatientRegisterRequestDTO request) {
        logger.debug("Registering patient with email: {}", request.email());
        String encodedPassword = passwordEncoder.encode(request.password());
        RegistrationStrategy<PatientRegisterRequestDTO, PatientRegisterResponseDTO> strategy = strategyProvider.getStrategy(Role.PATIENT);
        return strategy.register(request, encodedPassword);
    }
}
