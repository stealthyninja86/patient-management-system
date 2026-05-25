package com.pms.authservice.service;

import com.pms.authservice.dto.DepartmentDTO;
import com.pms.authservice.dto.HospitalDTO;
import com.pms.authservice.dto.LoginRequestDTO;
import com.pms.authservice.dto.LoginResponseDTO;
import com.pms.authservice.dto.RegisterRequestDTO;
import com.pms.authservice.dto.RegisterResponseDTO;
import com.pms.authservice.exception.InvalidCredentialsException;
import com.pms.authservice.factory.UserFactory;
import com.pms.authservice.grpc.HospitalGrpcClient;
import com.pms.authservice.grpc.PatientGrpcClient;
import com.pms.authservice.model.ProfileType;
import com.pms.authservice.model.User;
import com.pms.authservice.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final HospitalGrpcClient hospitalGrpcClient;
    private final PatientGrpcClient patientGrpcClient;
    private final UserFactory userFactory;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil, HospitalGrpcClient hospitalGrpcClient,
                       PatientGrpcClient patientGrpcClient, UserFactory userFactory) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.hospitalGrpcClient = hospitalGrpcClient;
        this.patientGrpcClient = patientGrpcClient;
        this.userFactory = userFactory;
    }

    public LoginResponseDTO authenticate(LoginRequestDTO loginRequestDTO) {
        logger.debug("Authenticating user: {}", loginRequestDTO.getEmail());
        User user = userService.findByEmail(loginRequestDTO.getEmail())
                .filter(u -> passwordEncoder.matches(loginRequestDTO.getPassword(), u.getPassword()))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        String role = user.getRole().name();
        String doctorId = user.getDoctorId();
        String patientId = user.getPatientId();

        String token;
        if (doctorId != null) {
            token = jwtUtil.generateToken(user.getEmail(), role, doctorId);
            return LoginResponseDTO.forDoctor(token, doctorId);
        }
        token = jwtUtil.generateToken(user.getEmail(), role);
        return LoginResponseDTO.forPatient(token, patientId);
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
    public RegisterResponseDTO register(RegisterRequestDTO request) {
        logger.debug("Registering user with email: {} and role: {}", request.email(), request.role());
        String encodedPassword = passwordEncoder.encode(request.password());
        User user = userFactory.createUser(request, encodedPassword);
        user = userService.createUser(user);

        switch (user.getRole()) {
            case DOCTOR:
                return registerDoctor(request, user);
            case PATIENT:
                return registerPatient(request, user);
            default:
                return new RegisterResponseDTO(null, null, user.getEmail(), "User registered successfully");
        }
    }

    private RegisterResponseDTO registerDoctor(RegisterRequestDTO request, User user) {
        hospital.DoctorResponse doctorResponse = hospitalGrpcClient.createDoctor(
                request.name(), request.email(), request.phone(),
                request.departmentId(), request.hospitalId());

        String doctorId = doctorResponse.getDoctorId();
        userFactory.createProfile(user, ProfileType.DOCTOR, doctorId);
        userService.createUser(user);

        return new RegisterResponseDTO(doctorId, null, user.getEmail(), "Doctor registered successfully");
    }

    private RegisterResponseDTO registerPatient(RegisterRequestDTO request, User user) {
        patient.PatientResponse patientResponse = patientGrpcClient.createPatient(
                request.name(), request.email(), request.phone(),
                request.address(), request.dateOfBirth(),
                request.gender(), request.bloodType());

        String patientId = patientResponse.getPatientId();
        userFactory.createProfile(user, ProfileType.PATIENT, patientId);
        userService.createUser(user);

        return new RegisterResponseDTO(null, patientId, user.getEmail(), "Patient registered successfully");
    }
}
