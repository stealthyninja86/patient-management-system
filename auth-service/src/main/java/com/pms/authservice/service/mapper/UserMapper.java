package com.pms.authservice.service.mapper;

import com.pms.authservice.dto.request.AdminRegisterRequestDTO;
import com.pms.authservice.dto.request.DoctorRegisterRequestDTO;
import com.pms.authservice.dto.request.PatientRegisterRequestDTO;
import com.pms.authservice.dto.response.AdminRegisterResponseDTO;
import com.pms.authservice.dto.response.DoctorRegisterResponseDTO;
import com.pms.authservice.dto.response.PatientRegisterResponseDTO;
import com.pms.authservice.model.ProfileType;
import com.pms.authservice.model.Role;
import com.pms.authservice.model.User;
import com.pms.authservice.model.UserProfile;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(AdminRegisterRequestDTO request, String encodedPassword) {
        User user = new User();
        user.setEmail(request.email());
        user.setPassword(encodedPassword);
        user.setRole(Role.ADMIN);
        return user;
    }

    public User toEntity(DoctorRegisterRequestDTO request, String encodedPassword, String doctorId) {
        User user = new User();
        user.setEmail(request.email());
        user.setPassword(encodedPassword);
        user.setRole(Role.DOCTOR);
        UserProfile profile = new UserProfile(user, ProfileType.DOCTOR, doctorId, null, request.hospitalId());
        user.addProfile(profile);
        return user;
    }

    public User toEntity(PatientRegisterRequestDTO request, String encodedPassword, String patientId) {
        User user = new User();
        user.setEmail(request.email());
        user.setPassword(encodedPassword);
        user.setRole(Role.PATIENT);
        UserProfile profile = new UserProfile(user, ProfileType.PATIENT, null, patientId, null);
        user.addProfile(profile);
        return user;
    }

    public AdminRegisterResponseDTO toAdminResponse(User user) {
        return new AdminRegisterResponseDTO(user.getEmail(), "Admin registered successfully");
    }

    public DoctorRegisterResponseDTO toDoctorResponse(User user, String doctorId) {
        return new DoctorRegisterResponseDTO(doctorId, user.getEmail(), "Doctor Registration Successful");
    }

    public PatientRegisterResponseDTO toPatientResponse(User user, String patientId) {
        return new PatientRegisterResponseDTO(patientId, user.getEmail(), "Patient Registration Successful");
    }
}