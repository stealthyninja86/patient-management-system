package com.pms.authservice.service.strategy;

import com.pms.authservice.dto.event.PatientRegisteredEvent;
import com.pms.authservice.dto.request.PatientRegisterRequestDTO;
import com.pms.authservice.dto.response.PatientRegisterResponseDTO;
import com.pms.authservice.grpc.PatientGrpcClient;
import com.pms.authservice.model.Role;
import com.pms.authservice.model.User;
import com.pms.authservice.service.UserService;
import com.pms.authservice.service.strategy.RegistrationEventPublisherStrategy;
import com.pms.authservice.service.mapper.UserMapper;
import com.pms.authservice.service.saga.RegistrationSaga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class PatientRegistrationStrategy implements RegistrationStrategy<PatientRegisterRequestDTO, PatientRegisterResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(PatientRegistrationStrategy.class);
    private final PatientGrpcClient patientGrpcClient;
    private final UserService userService;
    private final UserMapper userMapper;
    private final RegistrationEventPublisherStrategy eventPublisher;
    private final ObjectProvider<RegistrationSaga> sagaProvider;

    public PatientRegistrationStrategy(PatientGrpcClient patientGrpcClient,
                                       UserService userService, UserMapper userMapper,
                                       RegistrationEventPublisherStrategy eventPublisher,
                                       ObjectProvider<RegistrationSaga> sagaProvider
                                       ) {
        this.patientGrpcClient = patientGrpcClient;
        this.userService = userService;
        this.userMapper = userMapper;
        this.eventPublisher = eventPublisher;
        this.sagaProvider = sagaProvider;
    }

    @Override
    public PatientRegisterResponseDTO register(PatientRegisterRequestDTO request, String encodedPassword) {
        RegistrationSaga saga = sagaProvider.getObject();

        try {
            patient.PatientResponse patientResponse = patientGrpcClient.createPatient(
                    request.name(), request.email(), request.phone(),
                    request.address(), request.dateOfBirth(),
                    request.gender(), request.bloodType());

            saga.addCompensation(() -> {
                try {
                    patientGrpcClient.deletePatient(patientResponse.getPatientId());
                } catch (Exception e) {
                    log.error("Failed to delete patient during compensation for patientId {}", patientResponse.getPatientId(), e);
                }
            });

            User user = userMapper.toEntity(request, encodedPassword, patientResponse.getPatientId());
            saga.addCompensation(() -> userService.deleteUser(user));
            userService.saveUser(user);
            eventPublisher.publish(new PatientRegisteredEvent(
                    request.email(), patientResponse.getPatientId(), Instant.now()));

            saga.complete();
            return userMapper.toPatientResponse(user, patientResponse.getPatientId());
        }
        catch (Exception e) {
            saga.compensate();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Role supportedRole() {
        return Role.PATIENT;
    }
}