package com.pms.authservice.service;

import com.pms.authservice.dto.request.AdminRegisterRequestDTO;
import com.pms.authservice.dto.request.DoctorRegisterRequestDTO;
import com.pms.authservice.dto.request.LoginRequestDTO;
import com.pms.authservice.dto.request.PatientRegisterRequestDTO;
import com.pms.authservice.dto.response.AdminRegisterResponseDTO;
import com.pms.authservice.dto.response.DepartmentDTO;
import com.pms.authservice.dto.response.DoctorRegisterResponseDTO;
import com.pms.authservice.dto.response.HospitalDTO;
import com.pms.authservice.dto.response.LoginResponseDTO;
import com.pms.authservice.dto.response.PatientRegisterResponseDTO;
import com.pms.authservice.exception.InvalidCredentialsException;
import com.pms.authservice.grpc.HospitalGrpcClient;
import com.pms.authservice.model.Role;
import com.pms.authservice.model.User;
import com.pms.authservice.service.strategy.RegistrationStrategy;
import com.pms.authservice.service.strategy.RegistrationStrategyProvider;
import com.pms.authservice.service.util.JwtUtil;
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
    private final JwtUtil jwtUtil;
    private final HospitalGrpcClient hospitalGrpcClient;
    private final RegistrationStrategyProvider strategyProvider;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil, HospitalGrpcClient hospitalGrpcClient,
                       RegistrationStrategyProvider strategyProvider) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.hospitalGrpcClient = hospitalGrpcClient;
        this.strategyProvider = strategyProvider;
    }

    public LoginResponseDTO authenticate(LoginRequestDTO loginRequestDTO) {
        logger.debug("Authenticating user: {}", loginRequestDTO.email());
        User user = userService.findByEmail(loginRequestDTO.email())
                .filter(u -> passwordEncoder.matches(loginRequestDTO.password(), u.getPassword()))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        String role = user.getRole().name();
        String token;

        switch (user.getRole()) {
            case DOCTOR -> {
                String doctorId = user.getDoctorId();
                token = jwtUtil.generateToken(user.getEmail(), role, doctorId);
                return LoginResponseDTO.forDoctor(token, doctorId);
            }
            case PATIENT -> {
                String patientId = user.getPatientId();
                token = jwtUtil.generateToken(user.getEmail(), role,
                        patientId != null ? Map.of("patientId", patientId) : null);
                return LoginResponseDTO.forPatient(token, patientId);
            }
            default -> {
                token = jwtUtil.generateToken(user.getEmail(), role);
                return new LoginResponseDTO(token, role, null, null);
            }
        }
    }

    public boolean validateToken(String token) {
        logger.debug("Validating token");
        try {
            jwtUtil.validateToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
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
